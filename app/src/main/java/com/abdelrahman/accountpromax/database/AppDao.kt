package com.abdelrahman.accountpromax.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.abdelrahman.accountpromax.models.ClientBalanceUi
import com.abdelrahman.accountpromax.models.ClientEntity
import com.abdelrahman.accountpromax.models.ProjectEntity
import com.abdelrahman.accountpromax.models.TransactionEntity

@Dao
interface AppDao {
    @Query("SELECT COUNT(*) FROM projects")
    suspend fun projectsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("SELECT * FROM projects ORDER BY id ASC")
    fun observeProjects(): LiveData<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY id ASC")
    suspend fun getProjectsOnce(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Query("SELECT id FROM clients WHERE projectId = :projectId AND name = :name LIMIT 1")
    suspend fun findClientIdByName(projectId: Long, name: String): Long?

    @Query("SELECT * FROM clients WHERE id = :clientId LIMIT 1")
    suspend fun getClientById(clientId: Long): ClientEntity?

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("SELECT * FROM clients WHERE projectId = :projectId ORDER BY name ASC")
    fun observeClients(projectId: Long): LiveData<List<ClientEntity>>

    @Query(
        """
        SELECT c.id AS clientId, c.name AS clientName,
        SUM(CASE WHEN t.type = 'leh' THEN t.amount ELSE 0 END) AS lehTotal,
        SUM(CASE WHEN t.type = 'aleh' THEN t.amount ELSE 0 END) AS alehTotal
        FROM clients c
        LEFT JOIN transactions t ON c.id = t.clientId
        WHERE c.projectId = :projectId
        GROUP BY c.id, c.name
        ORDER BY (lehTotal - alehTotal) DESC
        """
    )
    fun observeClientBalances(projectId: Long): LiveData<List<ClientBalanceUi>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE clientId = :clientId ORDER BY date ASC, id ASC")
    fun observeTransactions(clientId: Long): LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE projectId = :projectId ORDER BY date ASC, id ASC")
    suspend fun getAllTransactions(projectId: Long): List<TransactionEntity>
}
