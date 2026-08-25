package com.example.util

import com.example.model.QuestStop

object WardLocationHelper {

    /**
     * Resolves the official Ward (Phường) location in Ho Chi Minh City
     * for a given stop name and coordinates, removing all district references.
     */
    fun getWardLocation(stop: QuestStop, lang: String): String {
        return getWardLocation(stop.name, lang, stop.latitude, stop.longitude)
    }

    fun getWardLocation(
        stopName: String,
        lang: String,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ): String {
        val nameLower = stopName.lowercase()

        return when {
            // Bách Khoa, Tô Hiến Thành, Lý Thường Kiệt -> Phường Diên Hồng, Quận 10
            nameLower.contains("bách khoa") || nameLower.contains("bach khoa") ||
            nameLower.contains("hcmut") || nameLower.contains("diên hồng") || nameLower.contains("dien hong") ||
            nameLower.contains("tô hiến thành") || nameLower.contains("to hien thanh") ||
            nameLower.contains("lý thường kiệt") || nameLower.contains("ly thuong kiet") -> {
                l(
                    lang,
                    "Phường Diên Hồng, Quận 10",
                    "Dien Hong Ward, District 10",
                    "延洪坊（第十郡）",
                    "ディエンホン坊（10区）",
                    "디엔홍동 (10군)"
                )
            }
            // Ký túc xá Bách Khoa / Hòa Hảo -> Phường 7, Quận 10
            nameLower.contains("hòa hảo") || nameLower.contains("hoa hao") -> {
                l(
                    lang,
                    "Phường 7, Quận 10",
                    "Ward 7, District 10",
                    "第十郡7坊",
                    "10区7坊",
                    "10군 7동"
                )
            }
            // Cư xá Lữ Gia -> Phường 15, Quận 11
            nameLower.contains("lữ gia") || nameLower.contains("lu gia") -> {
                l(
                    lang,
                    "Phường 15, Quận 11",
                    "Ward 15, District 11",
                    "第十一郡15坊",
                    "11区15坊",
                    "11군 15동"
                )
            }
            // Pasteur alley, Dong Khoi, Ly Tu Trong -> Phường Sài Gòn
            nameLower.contains("pasteur") || nameLower.contains("lý tự trọng") || nameLower.contains("ly tu trong") || nameLower.contains("bến nghé") || nameLower.contains("ben nghe") || nameLower.contains("đồng khởi") || nameLower.contains("dong khoi") -> {
                l(
                    lang,
                    "Phường Sài Gòn",
                    "Sai Gon Ward",
                    "西贡坊",
                    "サイゴン坊",
                    "사이공동"
                )
            }
            // Ton That Dam / Cau Ong Lanh / Pham Ngu Lao / Nguyen Trai -> Phường Cầu Ông Lãnh
            nameLower.contains("bến thành") || nameLower.contains("ben thanh") ||
            nameLower.contains("tôn thất đạm") || nameLower.contains("ton that dam") ||
            nameLower.contains("phạm ngũ lão") || nameLower.contains("pham ngu lao") ||
            nameLower.contains("cầu ông lãnh") || nameLower.contains("cau ong lanh") ||
            (nameLower.contains("nguyễn trãi") && !nameLower.contains("hội quán")) ||
            nameLower.contains("cơm tấm") -> {
                l(
                    lang,
                    "Phường Cầu Ông Lãnh",
                    "Cau Ong Lanh Ward",
                    "翁领桥坊",
                    "カウオンライン坊",
                    "까우옹란동"
                )
            }
            // Tan Dinh / Da Kao / Dang Dung / Do Phu Cafe -> Phường Tân Định
            nameLower.contains("đỗ phủ") || nameLower.contains("do phu") ||
            nameLower.contains("đặng dung") || nameLower.contains("dang dung") ||
            nameLower.contains("tân định") || nameLower.contains("tan dinh") ||
            nameLower.contains("đa kao") || nameLower.contains("da kao") ||
            nameLower.contains("nguyễn thị minh khai") || nameLower.contains("nguyen thi minh khai") -> {
                l(
                    lang,
                    "Phường Tân Định",
                    "Tan Dinh Ward",
                    "新定坊",
                    "タンディン坊",
                    "떤딘동"
                )
            }
            // Tu Xuong / Le Ngo Cat / Ngo Thoi Nhiem / Tran Quoc Thao / Ba Huyen Thanh Quan / Xa Loi / French Villas -> Phường Xuân Hòa
            nameLower.contains("tú xương") || nameLower.contains("tu xuong") ||
            nameLower.contains("lê ngô cát") || nameLower.contains("le ngo cat") ||
            nameLower.contains("ngô thời nhiệm") || nameLower.contains("ngo thoi nhiem") ||
            nameLower.contains("trần quốc thảo") || nameLower.contains("tran quoc thao") ||
            nameLower.contains("bà huyện thanh quan") || nameLower.contains("ba huyen thanh quan") ||
            nameLower.contains("xá lợi") || nameLower.contains("xa loi") ||
            nameLower.contains("vĩnh nghiêm") || nameLower.contains("vinh nghiem") ||
            nameLower.contains("xuân hòa") || nameLower.contains("xuan hoa") ||
            nameLower.contains("võ thị sáu") || nameLower.contains("vo thi sau") -> {
                l(
                    lang,
                    "Phường Xuân Hòa",
                    "Xuan Hoa Ward",
                    "春和坊",
                    "スアンホア坊",
                    "쑤언호아동"
                )
            }
            // Secret Commando Bunker Nguyen Dinh Chieu / Vo Van Tan / Ban Co -> Phường Bàn Cờ
            nameLower.contains("nguyễn đình chiểu") || nameLower.contains("nguyen dinh chieu") ||
            nameLower.contains("hầm vũ khí") || nameLower.contains("ham vu khi") ||
            nameLower.contains("biệt động") || nameLower.contains("biet dong") ||
            nameLower.contains("bàn cờ") || nameLower.contains("ban co") ||
            nameLower.contains("nguyễn thiện thuật") || nameLower.contains("võ văn tần") || nameLower.contains("vo van tan") -> {
                l(
                    lang,
                    "Phường Bàn Cờ",
                    "Ban Co Ward",
                    "棋盘坊",
                    "バンコー坊",
                    "반꺼동"
                )
            }
            // Cho Lon: Hao Si Phuong, Ba Lu Net Coffee, Phung Hung Herbal, Thien Hau Temple -> Phường Chợ Lớn / Phường Chợ Quán
            nameLower.contains("hào sĩ phường") || nameLower.contains("hao si phuong") ||
            (nameLower.contains("trần hưng đạo") && nameLower.contains("206")) -> {
                l(
                    lang,
                    "Phường Chợ Quán",
                    "Cho Quan Ward",
                    "曹关坊",
                    "チョークアン坊",
                    "쩌꽌동"
                )
            }
            nameLower.contains("ba lù") || nameLower.contains("ba lu") ||
            nameLower.contains("bùi hữu nghĩa") || nameLower.contains("bui huu nghia") ||
            nameLower.contains("thiên hậu") || nameLower.contains("thien hau") ||
            nameLower.contains("nghĩa an") || nameLower.contains("nghia an") ||
            nameLower.contains("triệu quang phục") || nameLower.contains("trieu quang phuc") ||
            nameLower.contains("phùng hưng") || nameLower.contains("phung hung") ||
            nameLower.contains("chợ lớn") || nameLower.contains("cho lon") -> {
                l(
                    lang,
                    "Phường Chợ Lớn",
                    "Cho Lon Ward",
                    "堤岸坊",
                    "チョロン坊",
                    "쩌롱동"
                )
            }
            // Hoa Binh / Phu Binh Lantern / Trinh Dinh Trong -> Phường Hòa Bình
            nameLower.contains("hòa bình") || nameLower.contains("hoa binh") ||
            nameLower.contains("trịnh đình trọng") || nameLower.contains("trinh dinh trong") ||
            nameLower.contains("phú bình") || nameLower.contains("phu binh") ||
            nameLower.contains("lồng đèn") || nameLower.contains("long den") ||
            nameLower.contains("161 lạc long quân") -> {
                l(
                    lang,
                    "Phường Hòa Bình",
                    "Hoa Binh Ward",
                    "和平坊",
                    "ホアビン坊",
                    "화빈동"
                )
            }
            // Binh Thoi / Lac Long Quan Crafts -> Phường Bình Thới
            nameLower.contains("bình thới") || nameLower.contains("binh thoi") ||
            nameLower.contains("341 lạc long quân") || nameLower.contains("lạc long quân") || nameLower.contains("lac long quan") -> {
                l(
                    lang,
                    "Phường Bình Thới",
                    "Binh Thoi Ward",
                    "平泰坊",
                    "ビンタイ坊",
                    "빈터이동"
                )
            }
            // Minh Phung / Phung Son Pagoda / 1408 3/2 -> Phường Minh Phụng
            nameLower.contains("minh phụng") || nameLower.contains("minh phung") ||
            nameLower.contains("phụng sơn") || nameLower.contains("phung son") ||
            nameLower.contains("hà tôn quyền") || nameLower.contains("ha ton quyen") ||
            nameLower.contains("sủi cảo") || nameLower.contains("sui cao") ||
            (nameLower.contains("3/2") && nameLower.contains("1408")) -> {
                l(
                    lang,
                    "Phường Minh Phụng",
                    "Minh Phung Ward",
                    "明凤坊",
                    "ミンフン坊",
                    "민풍동"
                )
            }
            // Net coffee / Phu Nhuan -> Phường Đức Nhuận
            nameLower.contains("phan đình phùng") || nameLower.contains("phan dinh phung") ||
            nameLower.contains("phú nhuận") || nameLower.contains("phu nhuan") -> {
                l(
                    lang,
                    "Phường Đức Nhuận",
                    "Duc Nhuan Ward",
                    "德润坊",
                    "ドックニュアン坊",
                    "득뉴안동"
                )
            }
            // Thanh Da / Cu Xa Thanh Da / Binh Quoi / Cau Kinh -> Phường Thanh Đa (Bán Đảo Thanh Đa)
            nameLower.contains("thanh đa") || nameLower.contains("thanh da") ||
            nameLower.contains("bình quới") || nameLower.contains("binh quoi") ||
            nameLower.contains("cư xá thanh đa") || nameLower.contains("cu xa thanh da") ||
            nameLower.contains("cầu kinh") || nameLower.contains("cau kinh") ||
            nameLower.contains("bán đảo thanh đa") || nameLower.contains("ban dao thanh da") -> {
                l(
                    lang,
                    "Phường Thanh Đa (Bán Đảo Thanh Đa)",
                    "Thanh Da Ward (Thanh Da Peninsula)",
                    "青多坊（青多半岛）",
                    "タインダー坊（タインダー半島）",
                    "탄다동 (탄다 반도)"
                )
            }
            // Coordinate based fallbacks
            latitude > 10.815 && latitude < 10.845 && longitude > 106.715 && longitude < 106.745 -> {
                l(
                    lang,
                    "Phường Thanh Đa (Bán Đảo Thanh Đa)",
                    "Thanh Da Ward (Thanh Da Peninsula)",
                    "青多坊（青多半岛）",
                    "タインダー坊（タインダー半島）",
                    "탄다동 (탄다 반도)"
                )
            }
            latitude > 10.770 && latitude < 10.785 && longitude > 106.695 && longitude < 106.710 -> {
                l(lang, "Phường Sài Gòn", "Sai Gon Ward", "西贡坊", "サイゴン坊", "사이공동")
            }
            latitude > 10.775 && latitude < 10.795 && longitude > 106.680 && longitude < 106.695 -> {
                l(lang, "Phường Xuân Hòa", "Xuan Hoa Ward", "春和坊", "スアンホア坊", "쑤언호아동")
            }
            latitude > 10.745 && latitude < 10.760 && longitude > 106.650 && longitude < 106.670 -> {
                l(lang, "Phường Chợ Lớn", "Cho Lon Ward", "堤岸坊", "チョロン坊", "쩌롱동")
            }
            else -> {
                l(lang, "Sài Gòn - TP. Hồ Chí Minh", "Saigon - Ho Chi Minh City", "西贡 - 胡志明市", "サイゴン・ホーチミン市", "사이공 - 호치민시")
            }
        }
    }
}
