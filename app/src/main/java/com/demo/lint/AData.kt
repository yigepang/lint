package com.demo.lint

import java.io.Serializable

/**
 *
 * @author pang
 * @date 2022/11/23 23:27
 */
data class AData(
    val data: BData,
    val cData: CData,
    val list:MutableList<BData>,
    val listA:List<BData>
) : Serializable {

    companion object{

    }

    data class CData(
        val name: String
    )
}
