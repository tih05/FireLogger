package com.tikhomirov.fire_logger.utils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal object JSONUtils {

    fun getObject(str: String, valuesToReplace: Map<String, String>? = null): Any {
        val obj = try {
            jsonToMap(JSONObject(str), valuesToReplace)
        } catch (e: JSONException) {
            try {
                convertToArray(str, valuesToReplace)
            } catch (e: JSONException) {
                str
            }
        }
        return obj!!
    }

    private fun convertToArray(str: String, valuesToReplace: Map<String, String>? = null): Any {
        val list = toList(JSONArray(str), valuesToReplace)
        return if (list.size == 1) {
            list[0]
        } else {
            list
        }
    }

    private fun jsonToMap(
        json: JSONObject,
        valuesToReplace: Map<String, String>? = null
    ): Map<String?, Any?>? {
        var retMap: Map<String?, Any?> = HashMap()
        if (json !== JSONObject.NULL) {
            retMap = toMap(json, valuesToReplace)
        }
        return retMap
    }

    private fun toMap(
        obj: JSONObject,
        valuesToReplace: Map<String, String>? = null
    ): Map<String?, Any?> {
        val map: MutableMap<String?, Any?> = HashMap()
        val keysItr: Iterator<String> = obj.keys()
        while (keysItr.hasNext()) {
            val key = keysItr.next()
            var value: Any = obj.get(key)
            when (value) {
                is JSONArray -> {
                    value = toList(value, valuesToReplace)
                }
                is JSONObject -> {
                    value = toMap(value, valuesToReplace)
                }
                JSONObject.NULL -> {
                    value = "null"
                }
            }
            if (valuesToReplace != null && valuesToReplace.containsKey(value)) {
                map[key] = valuesToReplace[value]
            } else {
                map[key] = value
            }
        }
        return map
    }

    private fun toList(array: JSONArray, valuesToReplace: Map<String, String>? = null): List<Any> {
        val list: MutableList<Any> = ArrayList()
        for (i in 0 until array.length()) {
            var value: Any = array.get(i)
            when (value) {
                is JSONArray -> {
                    value = toList(value)
                }
                is JSONObject -> {
                    value = toMap(value)
                }
            }
            if (valuesToReplace != null && valuesToReplace.containsKey(value)) {
                list.add(valuesToReplace[value]!!)
            } else {
                list.add(value)
            }
        }

        return list
    }
}