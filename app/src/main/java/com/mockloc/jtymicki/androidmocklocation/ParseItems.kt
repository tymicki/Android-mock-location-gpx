package com.mockloc.jtymicki.androidmocklocation

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class ParseItems {
    val items = ArrayList<TrackingPoint>()

    fun parse(xmlData: String): Boolean {
        var status = true
        var inItem = false
        var textValue = ""
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(xmlData.reader())
            var eventType = xpp.eventType
            var currentRecord = TrackingPoint()
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = xpp.name?.toLowerCase()
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName == "trkpt") {
                            inItem = true
                        }
                    }
                    XmlPullParser.TEXT -> textValue = xpp.text
                    XmlPullParser.END_TAG -> {
                        if (inItem) {
                            when (tagName) {
                                "trkpt" -> {
                                    val lat = xpp.getAttributeValue(null, "lat")
                                    items.add(currentRecord)
                                    inItem = false
                                    currentRecord = TrackingPoint()
                                }
//                                "lat" -> currentRecord.lat = textValue.toDouble()
//                                "lon" -> currentRecord.lon = textValue.toDouble()
                                "ele" -> currentRecord.ele = textValue.toDouble()
                                "time" -> currentRecord.trackingPointtime = textValue
                            }
                        }
                    }
                }
                eventType = xpp.next()
            }
        } catch (e: Exception) {
            status = false
        }
        return status
    }
}