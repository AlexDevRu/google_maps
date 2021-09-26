package com.example.data.repositories

import android.util.Log
import com.example.domain.exceptions.EmptyResultException
import com.example.domain.models.Markdown
import com.example.domain.repositories.IFirebaseRepository
import com.github.core.models.Location
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

class FirebaseRepository: IFirebaseRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MARKDOWNS_COLLECTION = "markdowns"

        private const val PLACE_ID_FIELD = "placeId"
        private const val NAME_FIELD = "name"
        private const val ADDRESS_FIELD = "address"
        private const val LOCATION_FIELD = "location"
        private const val CREATED_FIELD = "created"
    }

    private val store = Firebase.firestore
    private val auth = Firebase.auth

    init {
        store.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }

    override fun insertMarkdown(markdown: Markdown): Completable {
        return Completable.create { emitter ->
            store.collection(USERS_COLLECTION)
                .document(auth.currentUser!!.uid)
                .collection(MARKDOWNS_COLLECTION)
                .document(markdown.placeId)
                .set(mapOf(
                    PLACE_ID_FIELD to markdown.placeId,
                    NAME_FIELD to markdown.name,
                    ADDRESS_FIELD to markdown.address,
                    LOCATION_FIELD to GeoPoint(markdown.location!!.lat, markdown.location!!.lng),
                    CREATED_FIELD to Date().time,
                )).addOnSuccessListener {
                    Log.w("firebase", "insert markdown success")
                    emitter.onComplete()
                }.addOnFailureListener {
                    Log.w("firebase", "insert markdown error")
                    emitter.onError(it)
                }
        }
    }

    override fun deleteMarkdown(id: String): Completable {
        return Completable.create { emitter ->
            store.collection(USERS_COLLECTION)
                .document(auth.currentUser!!.uid)
                .collection(MARKDOWNS_COLLECTION)
                .whereEqualTo(PLACE_ID_FIELD, id)
                .get().addOnSuccessListener {
                    Log.w("firebase", "delete markdown success")
                    it.documents.forEach {
                        it.reference.delete()
                    }
                    emitter.onComplete()
                }.addOnFailureListener {
                    Log.w("firebase", "delete markdown error")
                    emitter.onError(it)
                }
        }
    }

    override fun getMarkdowns(): Single<List<Markdown>> {
        return Single.create { emitter ->
            store.collection(USERS_COLLECTION)
                .document(auth.currentUser!!.uid)
                .collection(MARKDOWNS_COLLECTION)
                .orderBy(CREATED_FIELD, Query.Direction.DESCENDING)
                .get().addOnSuccessListener {
                    val result = it.documents.map {
                        mapDocumentMarkdownToModel(it)
                    }
                    emitter.onSuccess(result)
                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    private fun mapDocumentMarkdownToModel(document: DocumentSnapshot): Markdown {
        val geoPoint = document.data?.get(LOCATION_FIELD) as GeoPoint

        return Markdown(
            placeId = document.data?.get(PLACE_ID_FIELD) as String,
            name = document.data?.get(NAME_FIELD) as String,
            address = document.data?.get(ADDRESS_FIELD) as String,
            location = Location(geoPoint.latitude, geoPoint.longitude)
        )
    }

    override fun isPlaceInMarkdowns(id: String): Single<Markdown> {
        return Single.create { emitter ->
            store.collection(USERS_COLLECTION)
                .document(auth.currentUser!!.uid)
                .collection(MARKDOWNS_COLLECTION)
                .whereEqualTo(PLACE_ID_FIELD, id)
                .get().addOnSuccessListener {
                    if(it.isEmpty)
                        emitter.onError(EmptyResultException())
                    else {
                        val document = it.documents[0]
                        emitter.onSuccess(mapDocumentMarkdownToModel(document))
                    }
                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }
}