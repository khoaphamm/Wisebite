package com.example.wisebitemerchant.data.model

data class CountryCode(
    val name: String,
    val code: String,
    val dialCode: String,
    val flag: String
) {
    override fun toString(): String = "$flag $name ($dialCode)"
}

object CountryCodeData {
    val countries = listOf(
        CountryCode("Vietnam", "VN", "+84", "🇻🇳"),
        CountryCode("United States", "US", "+1", "🇺🇸"),
        CountryCode("United Kingdom", "GB", "+44", "🇬🇧"),
        CountryCode("Singapore", "SG", "+65", "🇸🇬"),
        CountryCode("Malaysia", "MY", "+60", "🇲🇾"),
        CountryCode("Thailand", "TH", "+66", "🇹🇭"),
        CountryCode("Indonesia", "ID", "+62", "🇮🇩"),
        CountryCode("Philippines", "PH", "+63", "🇵🇭"),
        CountryCode("Japan", "JP", "+81", "🇯🇵"),
        CountryCode("South Korea", "KR", "+82", "🇰🇷"),
        CountryCode("China", "CN", "+86", "🇨🇳"),
        CountryCode("Australia", "AU", "+61", "🇦🇺"),
        CountryCode("Canada", "CA", "+1", "🇨🇦"),
        CountryCode("Germany", "DE", "+49", "🇩🇪"),
        CountryCode("France", "FR", "+33", "🇫🇷")
    )
    
    val defaultCountry = countries.first() // Vietnam as default
    
    fun getCountryByCode(code: String): CountryCode? {
        return countries.find { it.code == code }
    }
    
    fun getCountryByDialCode(dialCode: String): CountryCode? {
        return countries.find { it.dialCode == dialCode }
    }
}