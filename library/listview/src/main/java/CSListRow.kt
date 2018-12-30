package renetik.android.listview

import renetik.android.java.collections.list
import renetik.android.java.common.CSValues
import renetik.android.java.extensions.collections.deleteLast
import renetik.android.json.data.CSJsonData
import renetik.android.listview.CSListRow.RowTypes.*

fun <DataType : CSJsonData> createRow(type: CSListRow.RowTypes, index: Int): CSListRow<DataType> {
    return CSListRow(type, index)
}

fun <DataType : CSJsonData> createTableHeader(): CSListRow<DataType> {
    return CSListRow(TableHeader, 0)
}

fun <DataType : CSJsonData> createTableFooter(): CSListRow<DataType> {
    return CSListRow(TableFooter, 0)
}

fun <DataType : CSJsonData> createRow(data: DataType, viewType: CSListRow.RowTypes, index: Int)
        : CSListRow<DataType> {
    return CSListRow(data, viewType, index)
}

fun <T : CSJsonData> rowsFromList(data: List<T>): List<CSListRow<T>> {
    val listData = list<CSListRow<T>>(createTableHeader())
    var rowIndex = 0
    for (row in data) listData.add(createRow(row, Row, rowIndex++))
    listData.add(createTableFooter())
    return listData
}

fun <T : CSJsonData, Data> rowsFromListDatas(dataList: List<Data>)
        : List<CSListRow<Data>> where  Data : CSJsonData, Data : CSValues<T> {
    val listData = list<CSListRow<Data>>(createTableHeader())
    var sectionIndex = 0
    for (data in dataList) {
        listData.add(createRow(data, SectionHeader, sectionIndex))
        var rowIndex = 0
        while (rowIndex < data.values.size)
            listData.add(createRow(data, Row, rowIndex++))
        listData.add(createRow(data, SectionSpace, sectionIndex))
        sectionIndex++
    }
    listData.add(createTableFooter())
    return listData
}

fun <T : CSJsonData> rowsFromLists(data: List<List<T>>): List<CSListRow<T>> {
    val listData = list<CSListRow<T>>(createTableHeader())
    var sectionIndex = 0
    for (sectionData in data) {
        listData.add(createRow(SectionHeader, sectionIndex))
        var rowIndex = 0
        for (row in sectionData) listData.add(createRow(row, Row, rowIndex++))
        listData.add(createRow(SectionSpace, sectionIndex))
        sectionIndex++
    }
    listData.deleteLast()
    listData.add(createTableFooter())
    return listData
}

class CSListRow<DataType : CSJsonData> {

    val type: RowTypes
    val index: Int
    lateinit var data: DataType

    constructor(type: RowTypes, index: Int) {
        this.type = type
        this.index = index
    }

    constructor(data: DataType, type: RowTypes, index: Int) : this(type, index) {
        this.data = data
    }

    enum class RowTypes {
        TableHeader, Row, TableFooter, SectionHeader, SectionSpace
    }
}
