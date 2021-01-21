package com.dev.googlecloudtask

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.storage.Storage
import com.google.api.services.storage.StorageScopes
import com.google.api.services.storage.model.StorageObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URLConnection
import kotlin.coroutines.CoroutineContext

class CloudComputeViewModel(application: Application): AndroidViewModel(application), CoroutineScope{

    val sJob = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + sJob

    private var storage: Storage? = null
    private var ACCOUNT_ID_PROPERTY = "cloudtask@cloudtask-301706.iam.gserviceaccount.com"

    fun uploadFiles(bucketName: String?, filePath: String?, context: Context){
        launch {
            storage = getStorage(context)
            val `object` = StorageObject()
            `object`.bucket = bucketName
            val sdcard: File = Environment.getExternalStorageDirectory()
            val file = File(sdcard, filePath)
            val stream: InputStream = FileInputStream(File(filePath))
            try {
                val contentType: String = HttpURLConnection.guessContentTypeFromName(file.name)
                val content = InputStreamContent(contentType, stream)
                val insert = storage?.objects()?.insert(bucketName, null, content)
                insert?.name = file.name
                insert?.execute()
            } catch(ex: Exception) {
                ex.printStackTrace()
            }finally {
                stream.close()
            }
        }
    }

    private suspend fun getStorage(context: Context): Storage? {
        if (storage == null) {
            val httpTransport: HttpTransport = NetHttpTransport()
            val jsonFactory: JsonFactory = JacksonFactory()
            val scopes: MutableList<String> = ArrayList()
            scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL)
            val credential = GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(ACCOUNT_ID_PROPERTY)
                .setServiceAccountPrivateKeyFromP12File(getP12File(context))
                .setServiceAccountScopes(scopes).build()
            storage = Storage.Builder(
                httpTransport, jsonFactory,
                credential
            ).setApplicationName("CloudTask")
                .build()
        }
        return storage
    }

    private suspend fun getP12File(context: Context): File? {
        val pkc12Stream: InputStream = context.assets.open("cloudtask-301706-a44bc6e69016.p12")
        val tempPkc12File = File.createTempFile("temp_pkc12_file", "p12")
        val tempFileStream: OutputStream = FileOutputStream(tempPkc12File)
        var read = 0
        val bytes = ByteArray(1024)
        while (pkc12Stream.read(bytes).also { read = it } != -1) {
            tempFileStream.write(bytes, 0, read)
        }
        return tempPkc12File
    }

}