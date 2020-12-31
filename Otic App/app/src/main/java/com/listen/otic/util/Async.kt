package com.listen.otic.util

import android.os.AsyncTask

// TODO get rid of all of these

class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}

class doAsyncPost(val handler: () -> Unit, val postHandler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }

    override fun onPostExecute(result: Void?) {
        postHandler()
    }
}

class doAsyncPostWithResult<T>(val handler: () -> T?, val postHandler: (bitmap: T?) -> Unit) : AsyncTask<Void, Void, T>() {
    override fun doInBackground(vararg params: Void?): T? {
        return handler()
    }

    override fun onPostExecute(result: T?) {
        postHandler(result)
    }
}
