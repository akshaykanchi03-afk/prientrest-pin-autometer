package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testAmazonProductParsing() {
    val url = "https://www.amazon.com/Aesthetic-Workspace-Desk-Mat-Waterproof/dp/B09D8J6F95"
    
    // Test ASIN Extraction
    val asinRegex = """/(dp|gp/product|d)/(B[A-Z0-9]{9})""".toRegex()
    val asinMatch = asinRegex.find(url) ?: """\b(B[A-Z0-9]{9})\b""".toRegex().find(url)
    val asin = asinMatch?.groupValues?.get(2) ?: asinMatch?.groupValues?.get(1) ?: "B0PRODUCT"
    
    assertEquals("B09D8J6F95", asin)
    
    // Test Title Extraction
    val titleRegex = """amazon\.[a-z\.]+/([^/]+)/(dp|gp/product)/""".toRegex(RegexOption.IGNORE_CASE)
    val titleMatch = titleRegex.find(url)
    
    assertNotNull(titleMatch)
    val titleSlug = titleMatch!!.groupValues[1]
    assertEquals("Aesthetic-Workspace-Desk-Mat-Waterproof", titleSlug)
    
    val extractedTitle = titleSlug
        .replace("-", " ")
        .replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        
    assertEquals("Aesthetic Workspace Desk Mat Waterproof", extractedTitle)
    
    // Test monetization link build
    val associateTag = "myaffiliate-20"
    val monetizedUrl = "https://www.amazon.com/dp/$asin?tag=$associateTag"
    assertEquals("https://www.amazon.com/dp/B09D8J6F95?tag=myaffiliate-20", monetizedUrl)
  }
}
