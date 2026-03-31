package com.abdelrahman.accountpromax.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.abdelrahman.accountpromax.database.AppDatabase
import com.abdelrahman.accountpromax.database.AppRepository
import com.abdelrahman.accountpromax.models.ClientBalanceUi
import com.abdelrahman.accountpromax.models.ClientEntity
import com.abdelrahman.accountpromax.models.ProjectEntity
import com.abdelrahman.accountpromax.models.TransactionEntity
import com.abdelrahman.accountpromax.utils.BackupTransaction
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(AppDatabase.get(app).appDao())

    val projects: LiveData<List<ProjectEntity>> = repo.projects()
    val selectedProjectId = MediatorLiveData<Long>().apply { value = 1L }
    val selectedClientId = MediatorLiveData<Long>()
    val selectedClientName = MediatorLiveData<String>().apply { value = "" }
    val exportRows = MutableLiveData<List<BackupTransaction>>(emptyList())

    fun balances(projectId: Long): LiveData<List<ClientBalanceUi>> = repo.clientBalances(projectId)
    fun transactions(clientId: Long): LiveData<List<TransactionEntity>> = repo.transactions(clientId)

    fun ensureDefaultProject() = viewModelScope.launch {
        if (repo.projectsCount() == 0) repo.addProject("الدفتر الرئيسي")
    }

    fun addProject(name: String) = viewModelScope.launch { repo.addProject(name) }
    fun renameProject(projectId: Long, name: String) = viewModelScope.launch {
        val p = repo.getProjectsOnce().firstOrNull { it.id == projectId } ?: return@launch
        repo.renameProject(p.copy(name = name))
    }
    fun deleteProject(projectId: Long) = viewModelScope.launch {
        val projects = repo.getProjectsOnce()
        if (projects.size <= 1) return@launch
        val p = projects.firstOrNull { it.id == projectId } ?: return@launch
        repo.removeProject(p)
        val remain = repo.getProjectsOnce()
        if (remain.isNotEmpty() && selectedProjectId.value == projectId) {
            selectedProjectId.postValue(remain.first().id)
        }
    }

    fun selectProject(projectId: Long) {
        selectedProjectId.value = projectId
    }

    fun addTransaction(
        clientName: String,
        amount: Double,
        type: String,
        date: String,
        desc: String
    ) = viewModelScope.launch {
        val projectId = selectedProjectId.value ?: 1L
        val clientId = repo.addClient(projectId, clientName)
        repo.addTransaction(
            TransactionEntity(
                projectId = projectId,
                clientId = clientId,
                amount = amount,
                type = type,
                date = date,
                desc = desc
            )
        )
    }

    fun updateTransaction(tx: TransactionEntity) = viewModelScope.launch { repo.updateTransaction(tx) }
    fun deleteTransaction(tx: TransactionEntity) = viewModelScope.launch { repo.deleteTransaction(tx) }

    fun renameClient(clientId: Long, newName: String) = viewModelScope.launch {
        val current = repo.getClient(clientId) ?: return@launch
        repo.renameClient(current.copy(name = newName))
    }

    fun deleteClient(clientId: Long) = viewModelScope.launch {
        val current = repo.getClient(clientId) ?: return@launch
        repo.removeClient(current)
    }

    fun prepareExportData(onReady: (List<BackupTransaction>) -> Unit) = viewModelScope.launch {
        val projectId = selectedProjectId.value ?: 1L
        val rows = repo.allTransactions(projectId).map { tx ->
            val client = repo.getClient(tx.clientId)
            BackupTransaction(
                name = client?.name ?: "Unknown",
                amount = tx.amount,
                type = tx.type,
                date = tx.date,
                desc = tx.desc
            )
        }
        exportRows.postValue(rows)
        onReady(rows)
    }

    fun importRows(rows: List<BackupTransaction>) = viewModelScope.launch {
        val projectId = selectedProjectId.value ?: 1L
        rows.forEach { row ->
            val cid = repo.addClient(projectId, row.name)
            repo.addTransaction(
                TransactionEntity(
                    projectId = projectId,
                    clientId = cid,
                    amount = row.amount,
                    type = row.type,
                    date = row.date,
                    desc = row.desc
                )
            )
        }
    }
}
