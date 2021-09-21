package com.example.maps.utils

import android.graphics.Typeface
import ir.androidexception.datatable.DataTable
import ir.androidexception.datatable.model.DataTableHeader
import ir.androidexception.datatable.model.DataTableRow

object DataTableUtil {
    fun createTable(dataTable: DataTable, headers: List<String>?, rows: List<List<String?>>) {

        val headerBuilder = DataTableHeader.Builder()

        if(headers == null) {
            for(i in 0..rows.firstOrNull().orEmpty().size)
                headerBuilder.item(null, 1)

            dataTable.headerHorizontalPadding = 0f
            dataTable.headerVerticalPadding = 0f
            dataTable.headerTextSize = 0f
        } else {
            for(h in headers)
                headerBuilder.item(h, 1)
        }

        dataTable.header = headerBuilder.build()

        val _rows = ArrayList<DataTableRow>()

        for(row in rows) {
            val rowBuilder = DataTableRow.Builder()
            for(item in row) rowBuilder.value(item)
            _rows.add(rowBuilder.build())
        }

        dataTable.rows = _rows

        dataTable.typeface = Typeface.SANS_SERIF

        dataTable.inflate(dataTable.context)
    }
}