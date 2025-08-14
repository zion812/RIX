package com.rio.rostry.core.sync

import com.rio.rostry.core.database.entities.SyncMetadata
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class SyncConflictResolverTest {

    private val resolver = SyncConflictResolver()

    @Test
    fun localWinsWhenNewer() {
        val now = Date()
        val older = Date(now.time - 1000)
        val local = TestEntity("1", "local", SyncMetadata(updatedAt = now))
        val remote = TestEntity("1", "remote", SyncMetadata(updatedAt = older))

        val result = resolver.resolve(local, remote)
        require(result is Resolution.UseLocal)
        assertEquals("local", result.entity.value)
    }

    @Test
    fun remoteWinsWhenNewer() {
        val now = Date()
        val older = Date(now.time - 1000)
        val local = TestEntity("1", "local", SyncMetadata(updatedAt = older))
        val remote = TestEntity("1", "remote", SyncMetadata(updatedAt = now))

        val result = resolver.resolve(local, remote)
        require(result is Resolution.UseRemote)
        assertEquals("remote", result.entity.value)
    }

    @Test
    fun localUsedWhenNoRemote() {
        val local = TestEntity("1", "only_local", SyncMetadata(updatedAt = Date()))
        val result = resolver.resolve(local, null)
        require(result is Resolution.UseLocal)
        assertEquals("only_local", result.entity.value)
    }
}
