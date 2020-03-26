package cz.muni.fi.rpg.model.infrastructure

import com.firebase.ui.firestore.SnapshotParser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson

class GsonSnapshotParser<T>(private val modelClass: Class<T>) : SnapshotParser<T> {
    private val gson = Gson();

    override fun parseSnapshot(snapshot: DocumentSnapshot): T {
        return gson.fromJson(gson.toJsonTree(snapshot.data), modelClass);
    }
}