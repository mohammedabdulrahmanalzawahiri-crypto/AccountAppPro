package com.abdelrahman.accountpromax.database

import com.abdelrahman.accountpromax.models.ClientEntity
import com.abdelrahman.accountpromax.models.ProjectEntity
import com.abdelrahman.accountpromax.models.TransactionEntity

class AppRepository(private val dao: AppDao) {
    suspend fun projectsCount() = dao.projectsCount()
    fun projects() = dao.observeProjects()
    fun clients(projectId: Long) = dao.observeClients(projectId)
    fun clientBalances(projectId: Long) = dao.observeClientBalances(projectId)
    fun transactions(clientId: Long) = dao.observeTransactions(clientId)
    suspend fun allTransactions(projectId: Long) = dao.getAllTransactions(projectId)
    suspend fun getClient(clientId: Long) = dao.getClientById(clientId)

    suspend fun addProject(name: String) = dao.insertProject(ProjectEntity(name = name))
    suspend fun renameProject(project: ProjectEntity) = dao.updateProject(project)
    suspend fun removeProject(project: ProjectEntity) = dao.deleteProject(project)
    suspend fun getProjectsOnce() = dao.getProjectsOnce()

    suspend fun addClient(projectId: Long, name: String): Long {
        val existed = dao.findClientIdByName(projectId, name)
        if (existed != null) return existed
        return dao.insertClient(ClientEntity(projectId = projectId, name = name))
    }

    suspend fun addTransaction(tx: TransactionEntity) = dao.insertTransaction(tx)
    suspend fun updateTransaction(tx: TransactionEntity) = dao.updateTransaction(tx)
    suspend fun deleteTransaction(tx: TransactionEntity) = dao.deleteTransaction(tx)

    suspend fun renameClient(client: ClientEntity) = dao.updateClient(client)
    suspend fun removeClient(client: ClientEntity) = dao.deleteClient(client)
}
