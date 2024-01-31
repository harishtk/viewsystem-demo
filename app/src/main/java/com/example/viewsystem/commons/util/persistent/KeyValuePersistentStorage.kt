package com.example.viewsystem.commons.util.persistent

interface KeyValuePersistentStorage {
    fun writeDateSet(dataSet: KeyValueDataSet, removes: Collection<String>)
    fun getDataSet(): KeyValueDataSet
}