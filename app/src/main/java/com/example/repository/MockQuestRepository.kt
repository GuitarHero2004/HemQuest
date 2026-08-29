package com.example.repository

import com.example.model.Challenge
import com.example.model.GreenFactor
import com.example.model.GreenScore
import com.example.model.Quest
import com.example.model.QuestRequest
import com.example.model.QuestStop
import com.example.model.StopStatus

class MockQuestRepository {

    private fun l(
        lang: String,
        vi: String,
        en: String,
        zh: String,
        ja: String,
        ko: String
    ): String {
        return when (lang) {
            "vi" -> vi
            "zh" -> zh
            "ja" -> ja
            "ko" -> ko
            else -> en
        }
    }

    fun getFallbackQuest(request: QuestRequest): Quest {
        val lang = request.language
        val loc = (request.startingLocationName + " " + request.freeTextNotes + " " + request.interests.joinToString(" ")).lowercase()
        
        val questType = when {
            loc.contains("thanh đa") || loc.contains("thanh da") || loc.contains("cư xá thanh đa") || loc.contains("cu xa thanh da") || loc.contains("bình quới") || loc.contains("binh quoi") || loc.contains("bờ sông") || loc.contains("bo song") -> "q_thanhda"
            loc.contains("hòa bình") || loc.contains("hoa binh") || loc.contains("bình thới") || loc.contains("binh thoi") || loc.contains("minh phụng") || loc.contains("minh phung") || loc.contains("phú thọ") || loc.contains("phu tho") || loc.contains("lạc long quân") || loc.contains("phú bình") || loc.contains("lantern") || loc.contains("crafts") || loc.contains("lồng đèn") || loc.contains("11") -> "q11_crafts"
            loc.contains("bách khoa") || loc.contains("bach khoa") || loc.contains("hcmut") || loc.contains("tô hiến thành") || loc.contains("lý thường kiệt") || loc.contains("lữ gia") || loc.contains("q10") || loc.contains("quận 10") || loc.contains("sinh viên") -> {
                if (loc.contains("ẩm thực") || loc.contains("ăn vặt") || loc.contains("cơm tấm") || loc.contains("food") || loc.contains("snack")) {
                    "q10_bk_food"
                } else if (loc.contains("in ấn") || loc.contains("linh kiện") || loc.contains("maker") || loc.contains("đồ án")) {
                    "q10_bk"
                } else {
                    listOf("q10_bk", "q10_bk_food").random()
                }
            }
            loc.contains("bến vân đồn") || loc.contains("ben van don") || loc.contains("cầu mống") || loc.contains("cau mong") || loc.contains("quận 4") || loc.contains("q4") || loc.contains("vĩnh hội") -> "q4_riverfront"
            loc.contains("french") || loc.contains("colonial") || loc.contains("biệt thự") || loc.contains("xuân hòa") || loc.contains("xuan hoa") || loc.contains("nhiêu lộc") || loc.contains("nhieu loc") -> "q3_french"
            loc.contains("chợ lớn") || loc.contains("cho lon") || loc.contains("chợ quán") || loc.contains("cho quan") || loc.contains("an đông") || loc.contains("an dong") || loc.contains("street food") || loc.contains("ẩm thực") || loc.contains("sủi cảo") || loc.contains("hà tôn quyền") -> {
                if (loc.contains("thuốc bắc") || loc.contains("hào sĩ phường") || loc.contains("lương nhữ học") || loc.contains("herbal")) {
                    "q5_herbal"
                } else {
                    listOf("q5_food", "q5_herbal").random()
                }
            }
            loc.contains("commandos") || loc.contains("biệt động") || loc.contains("bunker") || loc.contains("bàn cờ") || loc.contains("ban co") || loc.contains("đỗ phủ") || loc.contains("phở bình") -> "q3_bunker"
            loc.contains("pasteur") || loc.contains("sài gòn") || loc.contains("sai gon") || loc.contains("tân định") || loc.contains("tan dinh") || loc.contains("bến thành") || loc.contains("ben thanh") || loc.contains("cầu ông lãnh") || loc.contains("cau ong lanh") -> "q1_alleys"
            else -> listOf("q_thanhda", "q10_bk", "q10_bk_food", "q11_crafts", "q3_french", "q5_food", "q5_herbal", "q3_bunker", "q4_riverfront", "q1_alleys").random()
        }

        val stops = when (questType) {
            "q_thanhda" -> listOf(
                QuestStop(
                    id = "stop_01_td",
                    placeId = "td_1",
                    name = l(lang, "Cư Xá Thanh Đa - Lô S & Quán Cà Phê Cổ (Bán Đảo Thanh Đa)", "Cư Xá Thanh Đa - Block S & Vintage Cafe (Thanh Da Peninsula)", "青多居舍S座与复古老咖啡馆（青多半岛）", "タインダー団地S棟＆レトロカフェ（タインダー半島）", "탄다 아파트 S동 & 빈티지 카페 (탄다 반도)"),
                    category = l(lang, "Chung Cư Di Sản", "Heritage Apartment Complex", "经典老式公寓", "レトロ公営住宅", "헤리티지 아파트 단지"),
                    latitude = 10.825800,
                    longitude = 106.724200,
                    whySelected = l(lang, "Khu cư xá kiểu mẫu đầu tiên của Sài Gòn thập niên 1970 với hành lang gió mát và sân chung thoáng đãng.", "Saigon's first model apartment complex from the 1970s with breezy corridors and tranquil central courtyards.", "建于1970年代的西贡首座样板住宅社区，通风走廊与绿树成荫的中庭极具年代感。", "1970年代に建てられたサイゴン初のモデル集合住宅。風通しの良い廊下と緑陰の中庭。", "1970년대 지어진 사이공 최초의 시범 주거단지. 시원한 복도와 푸른 중정."),
                    story = l(lang, "Những ô cửa chớp gỗ ngả màu thời gian, giàn hoa giấy rủ xuống từ ban công tầng 2 và nhịp sống bình dị hiếm có giữa lòng đô thị.", "Weathered wooden shutters, cascading bougainvillea from 2nd-floor balconies, and a rare slow-paced neighborhood rhythm.", "斑驳的百叶木窗、从二楼阳台垂落的盛开三角梅，以及城市中难得一见的恬静市井慢生活。", "時を経た木製ブラインド、バルコニーから垂れるブーゲンビリア、ゆったりとした時間。", "세월의 흔적이 묻어나는 나무 덧창, 발코니의 부겐빌레아, 여유로운 로컬 라이프."),
                    factReference = "Ký ức Đô thị Cư Xá Thanh Đa 1972",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh giàn hoa giấy hoặc mảng tường chung cư cổ Lô S", "Photo of bougainvillea or vintage Block S wall", "拍摄二楼阳台的三角梅或S座标志性复古外墙", "ブーゲンビリアまたはS棟のレトロな外壁を撮影", "부겐빌레아 꽃 또는 S동 클래식 외벽 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.CURRENT
                ),
                QuestStop(
                    id = "stop_02_td",
                    placeId = "td_2",
                    name = l(lang, "Hẻm Bờ Sông Thanh Đa & Công Viên Bờ Bán Đảo (P. Thanh Đa)", "Thanh Đa Riverside Alley & Peninsula Park", "青多河畔巷弄与半岛滨水绿道（青多坊）", "タインダー川沿いヘム＆ウォーターフロント公園（タインダー坊）", "탄다 강변 골목 & 반도 워터프론트 공원 (탄다동)"),
                    category = l(lang, "Cảnh Quan Bờ Sông", "Riverside Viewpoint", "水岸滨江景观", "リバーサイド遊歩道", "강변 뷰포인트"),
                    latitude = 10.828200,
                    longitude = 106.726500,
                    whySelected = l(lang, "Con hẻm mở ra toàn cảnh sông Sài Gòn lộng gió và mảng xanh bán đảo Thanh Đa.", "Alleyway opening directly onto expansive breezes of the Saigon River and lush peninsula greenery.", "直通西贡河岸的微风小径，坐拥开阔江景与青多半岛郁郁葱葱的原生绿意。", "サイゴン川の心地よい風と豊かな自然景観が広がる川沿いの隠れ小道。", "사이공 강의 시원한 바람과 탄다 반도의 푸른 자연이 펼쳐지는 골목."),
                    story = l(lang, "Nơi người dân tụ họp câu cá, ngắm hoàng hôn buông xuống dòng sông uốn quanh bán đảo.", "Where locals gather for leisurely fishing and watching glowing tropical sunsets over the meandering river bend.", "当地居民垂钓休闲、傍晚在夕阳余晖中闲话家常的惬意河畔空间。", "地元の人々が釣りを楽しんだり夕日を眺めたりする憩いの水辺スポット。", "현지인들이 낚시를 즐기고 강 너머로 지는 석양을 감상하는 평화로운 쉼터."),
                    factReference = "Bán đảo Sinh thái Thanh Đa - Bình Quới",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh mặt nước sông Sài Gòn hoặc hàng cây râm mát bờ sông", "Photo of river surface or shaded riverside trees", "拍摄波光粼粼的西贡河面或江边绿树成荫的长廊", "川面の波や木漏れ日の遊歩道写真を撮影", "사이공 강 수면 또는 그늘진 강변 가로수길 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_03_td",
                    placeId = "td_3",
                    name = l(lang, "Hẻm 101 Bình Quới & Lối Đi Bộ Bờ Kênh (P. Thanh Đa)", "Hẻm 101 Bình Quới & Canal Walking Path", "平贵101号小巷与运河静谧步行道（青多坊）", "ビンクオイ101番地ヘム＆運河散策路（タインダー坊）", "빈꾸오이 101번지 골목 & 운하 산책로 (탄다동)"),
                    category = l(lang, "Hẻm Vườn Sinh Thái", "Leafy Canal Alley", "运河生态小巷", "緑豊かな運河路地", "친환경 운하 골목"),
                    latitude = 10.830800,
                    longitude = 106.728600,
                    whySelected = l(lang, "Con hẻm rợp bóng dừa nước và vườn cây ăn trái, giữ nét thôn dã Nam Bộ ngay giữa lòng thành phố.", "Leafy lane shaded by water palms and fruit trees, preserving rural Southern charm inside the metropolis.", "保留着南统水椰与果树风貌的静谧绿巷，宛如大都市中隐秘的世外桃源。", "ヤシの木や果樹が生い茂る、大都市の中に残る南部の田園風景路地。", "물야자나무와 과일나무가 어우러진 대도시 속 남부 전통 전원 골목."),
                    story = l(lang, "Con đường rợp bóng mát nơi tiếng chim hót và gió sông làm dịu mát từng bước chân người tản bộ.", "A tranquil corridor where birdsong and river breezes refresh every mindful step.", "耳畔伴着清脆鸟鸣与江风低语，每一步漫步都倍感身心舒缓惬意。", "小鳥のさえずりと川のせせらぎが心地よい、癒やしのウォーキングルート。", "새소리와 강바람이 발걸음을 가볍게 해주는 힐링 보행로."),
                    factReference = "Hệ sinh thái Bán đảo Thanh Đa",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh tán lá dừa nước hoặc góc vườn xanh trong hẻm", "Photo of water palm leaves or green garden corner", "拍摄独特的野生水椰树叶或幽深庭院一角", "水ヤシの葉または庭園の緑を撮影", "물야자 잎 또는 초록 정원 한 컷 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_04_td",
                    placeId = "td_4",
                    name = l(lang, "Hẻm Chợ Thanh Đa & Quán Bánh Bèo Bến Sông (P. Thanh Đa)", "Thanh Đa Market Alley & Riverside Savory Cakes", "青多集市深巷与码头水蕨糕老铺（青多坊）", "タインダー市場ヘム＆蒸し米粉ケーキ屋台（タインダー坊）", "탄다 시장 골목 & 강변 반베오 맛집 (탄다동)"),
                    category = l(lang, "Ẩm Thực Chợ Quê", "Local Market Food", "传统市集风味", "下町市場グルメ", "전통 로컬 푸드"),
                    latitude = 10.824200,
                    longitude = 106.722800,
                    whySelected = l(lang, "Chợ cư xá lâu đời với các món ăn dân dã phương Nam như bánh bèo, bánh tằm bì và bún mắm.", "Historic residential market offering authentic Southern treats: savory steamed cakes, silkworm noodles, and fish broth.", "半个世纪历史的传统住宅区小菜场，汇集了地道水蕨糕、皮丝凉糕与浓香鱼汤米线。", "半世紀続く地元市場。蒸し餅や伝統の麺料理が手頃に味わえる。", "반세기 역사의 로컬 시장. 반베오와 남부 전통 국수를 맛볼 수 있는 곳."),
                    story = l(lang, "Những gánh hàng rong của các bà má Thanh Đa gắn bó với tuổi thơ bao thế hệ cư dân cư xá.", "Traditional street vendor baskets operated by Thanh Da matriarchs, cherished across generations.", "承载着青多半岛几代人儿时记忆的老阿嬷传统担头，烟火气十足。", "地元の年配女性たちが切り盛りする屋台。世代を超えて愛される懐かしい味。", "어머니들의 손맛이 깃든 전통 노점 길거리 음식의 따뜻한 풍경."),
                    factReference = "Văn hóa Ẩm thực Chợ Thanh Đa",
                    challenge = Challenge(prompt = l(lang, "Chụp đĩa bánh bèo chén hoặc quầy ẩm thực chợ quê", "Photo of savory steamed cake tray or market stall", "拍摄一碟晶莹剔透的水蕨糕或热闹的市集摊位", "蒸し米粉ケーキの小皿または市場の屋台を撮影", "작은 종지에 담긴 반베오 또는 시장 가판대 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_05_td",
                    placeId = "td_5",
                    name = l(lang, "Cầu Kinh Thanh Đa & Lối Vào Bán Đảo Xanh (P. Thanh Đa)", "Kinh Thanh Đa Bridge & Green Peninsula Gateway", "青多运河大桥与生态半岛入口（青多坊）", "タインダー運河橋＆グリーン半島ゲート（タインダー坊）", "탄다 운하 다리 & 생태 반도 관문 (탄다동)"),
                    category = l(lang, "Cửa Ngõ Di Sản", "Peninsula Gateway", "半岛历史地标", "歴史的ゲートウェイ", "반도 관문 랜드마크"),
                    latitude = 10.817800,
                    longitude = 106.719200,
                    whySelected = l(lang, "Cây cầu huyết mạch nối liền đô thị tấp nập với bán đảo sinh thái xanh Thanh Đa.", "The iconic bridge connecting bustling downtown Saigon to the lush green Thanh Da eco-peninsula.", "连接繁华喧嚣闹市与静谧生态绿洲青多半岛的标志性交通要道大桥。", "活気ある市街地と緑豊かなオアシス・タインダー半島を結ぶ象徴的な大橋。", "번화한 도심과 푸른 생태의 탄다 반도를 이어주는 상징적인 다리."),
                    story = l(lang, "Đứng trên cầu ngắm nhìn dòng kênh Thanh Đa đào từ năm 1897 đón tàu bè xuôi ngược.", "Overlooking the historic canal excavated in 1897 carrying peaceful boat traffic under tropical skies.", "俯瞰始凿于1897年、穿梭着往来船舶与悠久航运记忆的历史运河。", "1897年に開削された歴史ある運河を行き交う船を眺められるビュースポット。", "1897년 개통되어 배들이 오가던 유서 깊은 탄다 운하를 조망하는 다리."),
                    factReference = "Lịch sử Đào Kênh Thanh Đa 1897",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh dòng kênh hoặc góc nhìn cầu Kinh Thanh Đa", "Photo of canal waterway or Kinh Thanh Da bridge view", "拍摄波光粼粼的运河水面或青多运河大桥全貌", "運河の景色またはタインダー橋を撮影", "운하의 전경 또는 탄다 다리 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                )
            )
            "q10_bk" -> listOf(
                QuestStop(
                    id = "stop_01_bk",
                    placeId = "bk_1",
                    name = l(lang, "Hẻm 493 Tô Hiến Thành & Phố Ẩm Thực Sinh Viên BK (P. Diên Hồng, Q.10)", "493 Tô Hiến Thành Alley & BK Student Food (Dien Hong Ward, District 10)", "苏宪成493号巷与理工大学学生美食街（延洪坊，第十郡）", "トーヒエンタイン493番地ヘム＆工科大学生街グルメ（10区ディエンホン坊）", "토히엔탄 493번지 골목 & BK 대학가 먹거리 (10군 디엔홍동)"),
                    category = l(lang, "Hẻm Ẩm Thực Sinh Viên", "Student Food Alley", "学生美食小巷", "学生街グルメ路地", "대학가 먹거리 골목"),
                    latitude = 10.774500,
                    longitude = 106.662100,
                    whySelected = l(lang, "Con hẻm ẩm thực sầm uất gắn liền với sinh viên Bách Khoa TP.HCM tại Phường Diên Hồng, Quận 10.", "Bustling foodie alley deeply connected with HCMUT Bách Khoa students in Dien Hong Ward, District 10.", "汇聚了丰富实惠市井美食与理工学子青春回忆的热闹巷弄（第十郡延洪坊）。", "ホーチミン工科大学の学生たちで賑わう名物グルメ路地（10区ディエンホン坊）。", "호치민 공대 학생들의 추억과 저렴한 맛집이 가득한 활기찬 골목 (10군 디엔홍동)."),
                    story = l(lang, "Thiên đường ăn vặt với bánh tráng nướng, bún đậu mắm tôm, cơm tấm và trà tắc khổng lồ tại Phường Diên Hồng, Quận 10.", "Street snack paradise with grilled rice paper, tofu noodles, and giant iced lime tea in Dien Hong Ward, District 10.", "第十郡延洪坊著名的平价小吃天堂，烤米纸、豆腐米线与超大杯金桔茶香气四溢。", "ライスペーパー焼きや巨大アイスティーが人気の10区ディエンホン坊名物ストリートフード天国。", "구운 라이스페이퍼와 두부 국수, 대용량 깔라만시 차가 유명한 10구 디엔홍동 명물 골목."),
                    factReference = "Saigon Student Life Heritage (Dien Hong Ward, District 10)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh món ăn vặt sinh viên trong hẻm", "Photo of student snack in alley", "拍摄一张巷弄内诱人的学生平价小吃照片", "学生街のストリートフード写真を撮影", "골목 안 학생 간식 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.CURRENT
                ),
                QuestStop(
                    id = "stop_02_bk",
                    placeId = "bk_2",
                    name = l(lang, "Cổng Chính ĐH Bách Khoa & Giảng Đường A1-A4 (268 Lý Thường Kiệt, P. Diên Hồng, Q.10)", "HCMUT Main Campus & Historic Hallways (268 Ly Thuong Kiet, Dien Hong Ward, District 10)", "胡志明市理工大学主校区与学术主楼（李常杰268号，延洪坊，第十郡）", "ホーチミン工科大学メインキャンパス＆歴史講堂（10区ディエンホン坊）", "호치민 공과대학교 본관 & 역사 강의동 (10군 디엔홍동)"),
                    category = l(lang, "Di Sản Học Thuật & Kiến Trúc", "Academic Heritage", "高等学术殿堂", "学術・建築遺産", "학술 및 건축 유산"),
                    latitude = 10.772500,
                    longitude = 106.658200,
                    whySelected = l(lang, "Biểu tượng đại học kỹ thuật hàng đầu phương Nam tại 268 Lý Thường Kiệt, Phường Diên Hồng, Quận 10.", "The premier engineering university landmark at 268 Ly Thuong Kiet, Dien Hong Ward, District 10.", "坐落于李常杰路268号（第十郡延洪坊）的越南南方顶尖工科高等学府地标。", "10区ディエンホン坊リトゥオンキエット通りに位置する、南ベトナム最高峰の工科学術の殿堂。", "10군 디엔홍동 리트엉끼엣 268번지에 위치한 남부 최고의 명문 공과대학 상징."),
                    story = l(lang, "Thành lập từ năm 1957 (tiền thân Trung tâm Quốc gia Kỹ thuật Phú Thọ), nơi đào tạo hàng chục thế hệ kỹ sư tài ba.", "Established in 1957 (originally Phu Tho National Technical Center), nurturing generations of engineering talent.", "始建于1957年（前身为富寿国家技术中心），培育了数代卓越工程师人才。", "1957年創立（旧フートー国立技術センター）。何世代もの優れたエンジニアを育成してきた歴史ある校舎群。", "1957년 설립(구 푸토 국립기술센터)되어 수많은 엔지니어를 배출한 유서 깊은 캠퍼스."),
                    factReference = "Di sản Giáo dục ĐH Bách Khoa (268 Lý Thường Kiệt, P. Diên Hồng, Q.10)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh cổng trường hoặc hàng cây giảng đường A1-A4", "Photo of campus gate or tree-lined hall A1-A4", "拍摄校门或A1-A4学术主楼旁的绿荫大道", "キャンパス正門またはA1-A4講堂の並木道を撮影", "캠퍼스 정문 또는 A1-A4 강의동 가로수길 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_03_bk",
                    placeId = "bk_3",
                    name = l(lang, "Khu Cà Phê Đồ Án & In Ấn Bản Vẽ (Cư Xá Lữ Gia, P.15, Q.11)", "Lữ Gia Project Cafes & Blueprint Hub (Ward 15, District 11)", "吕嘉居舍自习咖啡馆与工程绘图打印街（第十一郡15坊）", "ルージア居留地製図カフェ＆図面印刷街（11区15坊）", "르자 지구 밤샘 과제 카페 & 도면 인쇄 거리 (11군 15동)"),
                    category = l(lang, "Cà Phê Đồ Án & Sáng Tạo", "Study Cafe Hub", "深夜自习咖啡", "製図・コワーキングカフェ", "과제 및 스터디 카페"),
                    latitude = 10.771200,
                    longitude = 106.654800,
                    whySelected = l(lang, "Khu phố cà phê 24/7 và xưởng in ấn bản vẽ gắn liền với các kỳ đồ án Bách Khoa tại Cư Xá Lữ Gia.", "24/7 cafe hubs and blueprint drafting shops where engineering projects come to life in Lữ Gia area.", "紧邻校区的24小时自习咖啡馆与大型图纸打印店聚集区（第十一郡15坊）。", "深夜まで学生たちが図面作成や議論に热中するカフェ＆製図街。", "밤샘 과제와 도면 출력을 위해 공대생들이 모이는 카페 거리."),
                    story = l(lang, "Nơi hàng ngàn kỹ sư tương lai thức thâu đêm thảo luận đồ án tốt nghiệp, tiếng máy in khổ A0 rào rào.", "Where thousands of future engineers stay up all night with CAD drawings and humming A0 plotters.", "无数未来工程师熬夜研讨毕业设计、A0大幅面绘图仪彻夜轰鸣的筑梦之地。", "CAD図面と格闘し、卒業制作を語り明かしたエンジニアたちの熱い青春の思い出。", "수천 명의 예비 엔지니어들이 졸업 작품을 준비하며 밤을 지새운 열정의 장소."),
                    factReference = "Saigon Engineering Hub (Lu Gia)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh không gian học tập hoặc tiệm in ấn bản vẽ", "Photo of study space or blueprint shop", "拍摄专注学习的咖啡角或专业图纸打印店", "学習スペースまたは図面印刷屋を撮影", "스터디 공간 또는 도면 인쇄소 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_04_bk",
                    placeId = "bk_4",
                    name = l(lang, "Hẻm 354 Lý Thường Kiệt & Ốc Sinh Viên (P. Diên Hồng, Q.10)", "354 Lý Thường Kiệt Alley & BK Street Seafood (Dien Hong Ward, District 10)", "李常杰354号巷与理工大学夜市炒螺美食（延洪坊，第十郡）", "リトゥオンキエット354番地ヘム＆貝料理屋台（10区ディエンホン坊）", "리트엉끼엣 354번지 골목 & BK 해산물 야시장 (10군 디엔홍동)"),
                    category = l(lang, "Hẻm Ốc & Ăn Vặt Đêm", "Late-Night Seafood", "深夜街头炒螺", "深夜の貝料理屋台", "심야 해산물 포차"),
                    latitude = 10.776100,
                    longitude = 106.657500,
                    whySelected = l(lang, "Điểm hẹn ẩm thực đêm quen thuộc của sinh viên và cựu sinh viên Bách Khoa tại Phường Diên Hồng, Quận 10.", "Iconic late-night seafood hangout for BK alumni and students in Dien Hong Ward, District 10.", "理工学子与校友们考完试后最爱相聚畅谈的深夜大排档巷弄（第十郡延洪坊）。", "テスト明けに学生たちが集まる、安くて美味しい名物の夜市（10区ディエンホン坊）。", "시험이 끝난 후 공대생들이 모여 회포를 푸는 정겨운 야시장 (10군 디엔홍동)."),
                    story = l(lang, "Mùi bơ tỏi, ốc móng tay xào rau muống và tiếng cười giòn tan sau những giờ thi căng thẳng.", "Aromas of garlic butter sautéed clams and cheerful laughter after exam weeks.", "黄油蒜蓉炒海鲜的诱人香气与考后如释重负的欢声笑语交织。", "ガーリックバターの香ばしい匂いと学生たちの明るい笑い声が響く。", "마늘 버터 조개볶음 향과 시험을 마친 학생들의 유쾌한 웃음소리."),
                    factReference = "Dien Hong Ward District 10 Culinary Heritage",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh chảo ốc nóng hổi hoặc không gian quán vỉa hè", "Photo of sizzling street dish or roadside dining", "拍摄热气腾腾的炒螺锅或露天排档画面", "熱々の屋台料理または露店の雰囲気を撮影", "지글지글 익어가는 해산물 요리 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_05_bk",
                    placeId = "bk_5",
                    name = l(lang, "Ký Túc Xá ĐH Bách Khoa & Sân Thể Thao Hòa Hảo (497 Hòa Hảo, P.7, Q.10)", "BK Student Dormitory & Sports Yard (497 Hoa Hao, Ward 7, District 10)", "理工大学学生宿舍区与运动场（和平路497号，第十郡7坊）", "工科大学生寮＆スポーツ広場（ホアハオ497番地、10区7坊）", "BK 기숙사 & 체육 광장 (호아하오 497번지, 10군 7동)"),
                    category = l(lang, "Ký Ức Sinh Viên", "Student Life Hub", "青春宿舍记忆", "学生生活の拠点", "대학 청춘의 요람"),
                    latitude = 10.760200,
                    longitude = 106.662500,
                    whySelected = l(lang, "Ký túc xá sinh viên đại học lớn bậc nhất Sài Gòn tại 497 Hòa Hảo, Phường 7, Quận 10.", "One of Saigon's largest university dorms at 497 Hoa Hao, Ward 7, District 10.", "胡志明市规模最大的大学宿舍群之一（和好路497号，第十郡7坊）。", "数千人の若き未来の技術者が共同生活を送る、活気あふれる巨大学生寮（10区7坊）。", "수천 명의 젊은 공학도들이 꿈을 키우는 사이공 최대 규모의 학생 기숙사 (10군 7동)."),
                    story = l(lang, "Không gian lưu giữ tình bạn tri kỷ, những buổi giao lưu guitar sân bóng và gánh tàu hũ nóng đêm khuya.", "Filled with lifelong friendships, courtyard guitar jams, and sweet late-night tofu carts.", "记录着深厚同窗挚友情谊、球场吉他弹唱与深夜暖胃甜豆腐脑摊的温馨回忆。", "夜のギター弾き語りや温かい豆腐スイーツ屋台が並ぶ、青春の温もり。", "동기들과의 끈끈한 우정, 달콤한 야식 순두부 카트가 있는 청춘의 추억."),
                    factReference = "BK Dormitory Archives (Ward 7, District 10)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh sân bóng rổ hoặc cổng ký túc xá", "Photo of basketball court or dorm entrance", "拍摄宿舍大门或充满朝气的篮球场", "寮のエントランスまたはバスケコートを撮影", "기숙사 정문 또는 농구장 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                )
            )
                        "q11_crafts" -> listOf(
                QuestStop(
                    id = "stop_01_q11",
                    placeId = "q11_1",
                    name = l(lang, "Hẻm 47 Trịnh Đình Trọng (Làng Lồng Đèn Phú Bình - P. Hòa Bình)", "Hẻm 47 Trịnh Đình Trọng (Phú Bình Lantern Alley - Hoa Binh Ward)", "郑庭重47号巷（富平传统灯笼巷 - 和平坊）", "チンディンチョン47番地ヘム（フービンランタン村 - ホアビン坊）", "찌엔딘쩌웅 47번지 골목 (푸빈 등불 마을 - 화빈동)"),
                    category = l(lang, "Làng Nghề Truyền Thống", "Traditional Crafts", "传统手工艺", "伝統工芸", "전통 공예"),
                    latitude = 10.763812,
                    longitude = 106.649231,
                    whySelected = l(lang, "Làng nghề làm lồng đèn giấy kiếng thủ công Phú Bình lâu đời tại Phường Hòa Bình.", "Historic handcrafted cellophane lantern crafting village in Hoa Binh Ward.", "位于和平坊的历史悠久的手工玻璃纸灯笼传统村落。", "ホアビン坊で古くから続く手作りランタンの職人街。", "화빈동의 역사 깊은 수공예 등불 마을."),
                    story = l(lang, "Nơi lưu giữ nét đẹp lồng đèn truyền thống Phú Bình qua nhiều thế hệ tại Phường Hòa Bình.", "Preserving the beauty of traditional lantern making for generations in Hoa Binh Ward.", "数代人坚守与传承着传统灯笼制作的高超技艺（和平坊）。", "ホアビン坊にて何世代にもわたって伝統的なランタン作りの技を継承。", "화빈동에서 수세대에 걸쳐 전통 등불 제작 기예를 보존해 온 곳."),
                    factReference = "Di sản Làng Lồng Đèn Phú Bình (Phường Hòa Bình)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh lồng đèn thủ công", "Photo of handcrafted lantern", "拍摄一张手工制作的彩色灯笼照片", "手作りランタンの写真を撮影", "수공예 등불 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.CURRENT
                ),
                QuestStop(
                    id = "stop_02_q11",
                    placeId = "q11_2",
                    name = l(lang, "Hẻm 161 Lạc Long Quân (Xưởng Thủ Công Mộc - P. Hòa Bình)", "Hẻm 161 Lạc Long Quân (Woodcraft Workshops Alley - Hoa Binh Ward)", "雒龙君161号巷（木雕精细作坊 - 和平坊）", "ラックロングアン161番地ヘム（木工工房路地 - ホアビン坊）", "락롱꾸언 161번지 골목 (목공예 공방 - 화빈동)"),
                    category = l(lang, "Thủ Công Mộc & Gốm", "Craft Workshops", "木艺与陶瓷", "木工＆陶芸工房", "목공 및 도자기 공방"),
                    latitude = 10.765100,
                    longitude = 106.651000,
                    whySelected = l(lang, "Tập trung các xưởng thủ công gỗ và trang trí tại Phường Hòa Bình.", "Concentration of woodworking and decor workshops in Hoa Binh Ward.", "汇聚了诸多精湛的木雕与复古家居手作坊（和平坊）。", "ホアビン坊の木工やレトロインテリアの職人工房が集まる路地。", "화빈동의 목공 및 빈티지 공방들이 모여있는 정겨운 골목."),
                    story = l(lang, "Những âm thanh bào gỗ và mùi thơm gỗ tự nhiên đậm chất hẻm thủ công tại Phường Hòa Bình.", "Aromas of natural wood and sounds of carving echoing through the alley in Hoa Binh Ward.", "小巷深处弥漫着天然木香与匠人们凿木制作的韵律声（和平坊）。", "木を削る音と天然木の香りが漂うサイゴンの職人路地（ホアビン坊）。", "나무를 깎는 소리와 자연 목재 향이 가득한 사이공 골목 (화빈동)."),
                    factReference = l(lang, "Di sản xưởng thủ công Phường Hòa Bình", "Hoa Binh Ward Traditional Craft Heritage", "和平坊传统手工艺遗产", "ホアビン坊伝統工芸遺産", "화빈동 전통 공예 유산"),
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh sản phẩm gỗ thủ công", "Photo of wooden craft piece", "拍摄一件雕刻精美的木质手工艺品", "木工芸品の写真を撮影", "목공예품 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_03_q11",
                    placeId = "q11_3",
                    name = l(lang, "Hẻm 341 Lạc Long Quân (Gốm Sứ & Trang Trí - P. Bình Thới)", "Hẻm 341 Lạc Long Quân (Ceramics & Decor Alley - Binh Thoi Ward)", "雒龙君341号巷（陶瓷装饰艺术巷 - 平泰坊）", "ラックロングアン341番地ヘム（陶芸装飾路地 - ビンタイ坊）", "락롱꾸언 341번지 골목 (도자기 공예 골목 - 빈터이동)"),
                    category = l(lang, "Gốm Sứ & Trang Trí", "Ceramics & Decor", "陶瓷与装饰艺术", "陶芸＆装飾", "도자기 및 장식 공예"),
                    latitude = 10.762100,
                    longitude = 106.648500,
                    whySelected = l(lang, "Không gian trưng bày gốm mộc và nghệ thuật trang trí thủ công tại Phường Bình Thới.", "Display space of handmade ceramics and artisanal decor in Binh Thoi Ward.", "位于平泰坊的质朴手工陶瓷与传统装饰艺术展示空间。", "ビンタイ坊の手作り陶芸と伝統装飾アートの空間。", "빈터이동의 수제 도자기와 전통 공예 전시 공간."),
                    story = l(lang, "Nơi kết nối giữa nghệ thuật gốm phương Nam và nhịp sống dân dã tại Phường Bình Thới.", "Connecting Southern ceramic art with peaceful alley life in Binh Thoi Ward.", "连接着南方陶瓷艺术韵味与平泰坊淳朴祥和的巷弄生活。", "南部陶芸の美とビンタイ坊の穏やかな暮らしが調和する場所。", "남부 도자기 예술과 빈터이동 골목의 소박한 정취가 어우러진 곳."),
                    factReference = "Di sản Gốm Thủ Công Phường Bình Thới",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh bức tường hoa gốm", "Photo of ceramic wall motif", "拍摄古朴精美的陶瓷浮雕墙壁", "陶器タイルの壁画を撮影", "도자기 타일 벽 장식 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_04_q11",
                    placeId = "q11_4",
                    name = l(lang, "Hẻm 1408 Đường 3/2 (Lối Vô Chùa Phụng Sơn - P. Minh Phụng)", "Hẻm 1408 3/2 (Phụng Sơn Pagoda Entrance - Minh Phung Ward)", "3月2日路1408号巷（凤山寺入口巷 - 明凤坊）", "3月2日通り1408番地ヘム（フンソン寺参道 - ミンフン坊）", "3월 2일 거리 1408번지 골목 (풍선 사원 입구 - 민풍동)"),
                    category = l(lang, "Di Tích Lịch Sử", "Historic Landmark", "历史古迹", "歴史的記念碑", "역사 유적지"),
                    latitude = 10.758800,
                    longitude = 106.650100,
                    whySelected = l(lang, "Con hẻm dẫn vào ngôi chùa cổ xây từ đầu thế kỷ 19 tại Phường Minh Phụng.", "Alley leading to an ancient pagoda dating back to the early 19th century in Minh Phung Ward.", "通往始建于19世纪初国家级古老寺院的幽静小巷（明凤坊）。", "ミンフン坊の19世紀初頭の由緒ある古刹へと続く参道。", "민풍동의 19세기 초 건립된 유서 깊은 고찰 진입 골목."),
                    story = l(lang, "Ngôi cổ tự thanh tịnh nằm ẩn mình sau những ngôi nhà hẻm mộc mạc tại Phường Minh Phụng.", "Serene ancient pagoda nestled quietly behind humble alley houses in Minh Phung Ward.", "远离喧嚣的清幽古寺静静隐匿在明凤坊淳朴祥和的巷弄深处。", "素朴な路地裏の民家の奥に佇む、静寂に包まれた歴史ある古寺（ミンフン坊）。", "민풍동 소박한 골목 주택들 뒤편에 고즈넉이 자리한 고찰."),
                    factReference = l(lang, "Di tích Kiến trúc Quốc gia Chùa Phụng Sơn (Phường Minh Phụng)", "Phung Son Pagoda National Heritage (Minh Phung Ward)", "凤山寺国家级建筑艺术遗迹（明凤坊）", "フンソン寺国家建築遺産（ミンフン坊）", "풍선 사원 국가 건축 유산 (민풍동)"),
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh cổng tam quan chùa trong hẻm", "Photo of pagoda gate in alley", "拍摄隐藏在巷弄深处的古朴寺庙山门", "路地裏に佇む寺院の山門を撮影", "골목 안 고찰 일주문 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                )
            )
            "q3_french" -> listOf(
                QuestStop(
                    id = "stop_01_q3f",
                    placeId = "q3f_1",
                    name = l(lang, "Hẻm 10 Nguyễn Thông (Hàng Rào Biệt Thự Cổ - P. Xuân Hòa)", "Hẻm 10 Nguyễn Thông (Vintage Villa Alley - Xuan Hoa Ward)", "阮通10号巷（经典法式别墅巷 - 春和坊）", "グエントン10番地ヘム（レトロ洋館路地 - スアンホア坊）", "응우옌통 10번지 골목 (빈티지 빌라 골목 - 쑤언호아동)"),
                    category = l(lang, "Kiến Trúc Pháp Cổ", "Colonial Heritage", "法式经典建筑", "コロニアル建築", "프랑스식 고택"),
                    latitude = 10.783400,
                    longitude = 106.687200,
                    whySelected = l(lang, "Dãy tường rào hoa văn sắt và kiến trúc thuộc địa đặc trưng tại Phường Xuân Hòa.", "Wrought-iron gates and distinct colonial villa architecture in Xuan Hoa Ward.", "位于春和坊的独特铸铁花饰栏杆与标志性法式古典别墅群落。", "スアンホア坊のコロニアル洋館建築が残る路地。", "쑤언호아동의 고풍스러운 철제 울타리와 프랑스식 빌라 건축 양식."),
                    story = l(lang, "Khu vực từng là nơi ở của công chức và trí thức Sài Gòn đầu thế kỷ 20 tại Phường Xuân Hòa.", "Historic residential enclave of early 20th-century Saigon scholars in Xuan Hoa Ward.", "20世纪初西贡学者文人与法式公务宅邸的聚集街区（春和坊）。", "20世紀初頭の知識人や官僚が暮らした歴史ある邸宅街（スアンホア坊）。", "20세기 초 사이공 학자들과 관료들의 역사적인 주거지 (쑤언호아동)."),
                    factReference = l(lang, "Hồ sơ Di sản Kiến trúc Phường Xuân Hòa", "Xuan Hoa Ward Architectural Heritage Archive", "春和坊建筑遗产档案", "スアンホア坊建築遺産アーカイブ", "쑤언호아동 건축 유산 아카이브"),
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh hoa văn cửa sắt cổ", "Photo of vintage wrought iron gate", "拍摄精美复古铁艺大门纹样", "ヴィンテージの鉄製門扉の写真を撮影", "빈티지 철제 문양 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.CURRENT
                ),
                QuestStop(
                    id = "stop_02_q3f",
                    placeId = "q3f_2",
                    name = l(lang, "Hẻm 280 Cách Mạng Tháng 8 (Biệt Thự Vườn - P. Nhiêu Lộc)", "Hẻm 280 CMT8 (Heritage Garden Alley - Nhieu Loc Ward)", "八月革命280号巷（花园古洋楼 - 饶禄坊）", "カックマンタンタム280番地ヘム（庭園洋館路地 - ニエウロック坊）", "깍망탕땀 280번지 골목 (가든 빌라 골목 - 니에우록동)"),
                    category = l(lang, "Biệt Thự Vườn", "Garden Villa", "花园洋房", "ガーデンヴィラ", "가든 빌라"),
                    latitude = 10.782100,
                    longitude = 106.678900,
                    whySelected = l(lang, "Khu vườn cổ kính ẩn sau con hẻm yên tĩnh tại Phường Nhiêu Lộc.", "Secluded heritage garden tucked inside a quiet alley in Nhieu Loc Ward.", "隐匿于饶禄坊静谧小巷深处的百年花园老宅。", "ニエウロック坊の静かな路地的奥に佇む歴史ある庭園と古民家。", "니에우록동 골목 안쪽에 숨겨진 유서 깊은 정원 주택."),
                    story = l(lang, "Lưu giữ nếp sống thanh lịch của Sài Gòn xưa với giàn hoa giấy rực rỡ bên kênh Nhiêu Lộc.", "Preserving elegant old Saigon charm adorned with vibrant bougainvillea in Nhieu Loc Ward.", "满墙盛开的三角梅见证了老西贡温婉从容的闲适时光（饶禄坊）。", "ブーゲンビリアが咲き誇る、古き良きサイゴンの優雅な暮らし（ニエウロック坊）。", "만개한 부겐빌레아가 반겨주는 옛 사이공의 우아한 라이프스타일 (니에우록동)."),
                    factReference = "Saigon Heritage Villas (Nhieu Loc Ward)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh giàn hoa giấy cổ", "Photo of bougainvillea trellis", "拍摄老洋房前盛开的三角梅", "ブーゲンビリアの花の写真を撮影", "부겐빌레아 꽃 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_03_q3f",
                    placeId = "q3f_3",
                    name = l(lang, "Hẻm 35 Ngô Thời Nhiệm (Góc Xanh Tĩnh Lặng - P. Xuân Hòa)", "Hẻm 35 Ngô Thời Nhiệm (Shaded Green Alley - Xuan Hoa Ward)", "吴时任35号巷（绿荫蔽日静谧巷弄 - 春和坊）", "ゴーテイニエム35番地ヘム（緑陰路地 - スアンホア坊）", "응오쩌이념 35번지 골목 (도심 숲 골목 - 쑤언호아동)"),
                    category = l(lang, "Không Gian Xanh", "Green Urban Pocket", "城市绿色静角", "緑豊かな路地裏", "도심속 푸른 골목"),
                    latitude = 10.780100,
                    longitude = 106.693100,
                    whySelected = l(lang, "Con hẻm yên bình nổi tiếng với cây xanh rợp bóng tại Phường Xuân Hòa.", "Peaceful alley famous for its lush canopy and artsy vibe in Xuan Hoa Ward.", "位于春和坊郁郁葱葱的树冠遮蔽与文艺氛围巷弄。", "スアンホア坊の緑の木々が覆い尽くすアートな雰囲気漂う路地。", "쑤언호아동의 아름다운 나무 그늘과 예술적 감성이 어우러진 골목."),
                    story = l(lang, "Nơi gặp gỡ của giới nghệ thuật, âm nhạc và nhiếp ảnh tại trung tâm Phường Xuân Hòa.", "A beloved gathering space for arts, music, and local photographers in Xuan Hoa Ward.", "聚集了诸多文人雅士、音乐家与摄影爱好者的灵感空间（春和坊）。", "アーティストや写真家が集うサイゴンの文化スポット（スアンホア坊）。", "예술가와 사진가들이 즐겨 찾는 사이공의 문화 골목 (쑤언호아동)."),
                    factReference = "Green Saigon Alley Survey (Xuan Hoa Ward)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh vệt nắng qua tán cây hẻm", "Photo of sunlight through alley trees", "拍摄阳光穿过巷弄树叶斑驳的光影", "木漏れ日が差し込む路地の風景を撮影", "나뭇잎 사이로 내리쬐는 햇살 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_04_q3f",
                    placeId = "q3f_4",
                    name = l(lang, "Hẻm 142 Trần Quốc Thảo (Nhà Cổ Mộc Mỹ Nghệ - P. Xuân Hòa)", "Hẻm 142 Trần Quốc Thảo (Wooden Heritage Manor - Xuan Hoa Ward)", "陈国草142号巷（古韵木雕大宅 - 春和坊）", "チャンクオクタオ142番地ヘム（伝統木工古民家 - スアンホア坊）", "쩐꾸옥따오 142번지 골목 (목공예 고택 - 쑤언호아동)"),
                    category = l(lang, "Kiến Trúc Mộc", "Wooden Heritage", "木雕古宅", "木造コロニアル建築", "목조 고택 및 공예"),
                    latitude = 10.784500,
                    longitude = 106.688500,
                    whySelected = l(lang, "Ngôi biệt thự gỗ với đường nét chạm khắc tinh xảo tại Phường Xuân Hòa.", "Wooden manor showing intricate hand carved woodwork in Xuan Hoa Ward.", "位于春和坊具有独特古典木质门窗与精美雕花的经典老宅。", "スアンホア坊の木造洋館のエレガントな邸宅。", "쑤언호아동의 정교한 목조 조각과 빈티지 도어가 돋보이는 고택."),
                    story = l(lang, "Nối liền lịch sử xưởng gỗ xưa và văn hóa trà tại Phường Xuân Hòa.", "Connecting old carpentry traditions with tranquil tea culture in Xuan Hoa Ward.", "完美融和了传统木工名匠底蕴与静心品茗的优雅生活（春和坊）。", "伝統的な木工職人の技と紅茶・茶道文化が融合（スアンホア坊）。", "전통 목공예와 차 문화가 오롯이 스며든 특별한 공간 (쑤언호아동)."),
                    factReference = "HCMC Architecture Inventory (Xuan Hoa Ward)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh chi tiết gỗ chạm khắc", "Photo of carved wood detail", "拍摄门窗上精美的复古木雕纹理", "木彫り装飾のディテールを撮影", "정교한 목조 장식 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_05_q3f",
                    placeId = "q3f_5",
                    name = l(lang, "Hẻm 89 Bà Huyện Thanh Quan (Chùa Xá Lợi - P. Xuân Hòa)", "Hẻm 89 Bà Huyện Thanh Quan (Xá Lợi Entrance Alley - Xuan Hoa Ward)", "婆县清关89号巷（舍利寺入口巷弄 - 春和坊）", "バフェンタアクアン89番地ヘム（シャーロイ寺参道 - スアンホア坊）", "바후엔타인꾸안 89번지 골목 (샤로이 사원 입구 - 쑤언호아동)"),
                    category = l(lang, "Di Tích Kiến Trúc", "Architectural Landmark", "历史宗教建筑", "歴史的仏教寺院", "역사적 불교 사원"),
                    latitude = 10.778800,
                    longitude = 106.686500,
                    whySelected = l(lang, "Tháp chuông 7 tầng cao nhất Sài Gòn và kiến trúc hiện đại tại Phường Xuân Hòa.", "Famous 7-storey bell tower, highest in Saigon, in Xuan Hoa Ward.", "位于春和坊拥有西贡最高的7层钟楼与独特的印支风尚佛寺建筑。", "スアンホア坊のサイゴン最高峰の7層の鐘楼を誇る近代仏教建築の名所。", "쑤언호아동의 사이공에서 가장 높은 7층 종탑을 품은 명성 높은 사원."),
                    story = l(lang, "Xây dựng từ năm 1956, chứng nhân cho nhiều sự kiện lịch sử văn hóa tại Phường Xuân Hòa.", "Built in 1956, bearing witness to important cultural & historical events in Xuan Hoa Ward.", "建于1956年，见证了许多重大的历史文化变迁（春和坊）。", "1956年建立、サイゴンの数々の歴史的瞬間を見届けてきた名刹。", "1956년에 건립되어 사이공의 주요 역사적 순간을 함께한 명소."),
                    factReference = "Xá Lợi Temple Cultural Register (Xuan Hoa Ward)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh tháp chuông nhìn từ trong hẻm", "Photo of bell tower seen from alley", "拍摄从巷弄抬头望向巍峨钟楼的角度", "路地裏から見上げる鐘楼の写真を撮影", "골목에서 바라본 종탑 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                )
            )
            "q5_food" -> listOf(
                QuestStop(
                    id = "stop_01_q5",
                    placeId = "q5_1",
                    name = l(lang, "Hẻm Sủi Cảo Hà Tôn Quyền (P. Chợ Lớn)", "Hà Tôn Quyền Dumpling Alley (Cho Lon Ward)", "何孙权水饺一条街（堤岸坊）", "ハトンクエン水餃子路地（チョロン坊）", "하똔꾸엔 물만두 골목 (쩌롱동)"),
                    category = l(lang, "Hẻm Ẩm Thực Người Hoa", "Chinese Dumpling Alley", "华埠水饺名巷", "中華水餃子通り", "화교 만두 골목"),
                    latitude = 10.756200,
                    longitude = 106.654800,
                    whySelected = l(lang, "Con hẻm ẩm thực sủi cảo lâu đời và sầm uất bậc nhất Chợ Lớn.", "One of the most famous and bustling traditional dumpling corridors in Cho Lon.", "堤岸历史悠久、香气四溢的传统广式水饺名巷。", "チョロンで最も有名で活気あふれる伝統の水餃子屋台街。", "쩌롱에서 가장 유서 깊고 활기찬 전통 물만두 골목."),
                    story = l(lang, "Nồi nước dùng ninh từ tôm khô, mực nướng và những chiếc sủi cảo tôm thịt căng mọng thơm nức.", "Simmering broths made from dried shrimp and squid, serving plump prawn dumplings.", "鲜虾干贝慢火熬制的鲜美高汤，搭配皮薄馅大、弹牙爽口的鲜虾水饺。", "干しエビとイカの出汁が効いた絶品スープとプリプリの海老水餃子。", "건새우와 오징어로 우려낸 깊은 육수에 탱글탱글한 새우만두."),
                    factReference = "Cho Lon Culinary Guild",
                    challenge = Challenge(prompt = l(lang, "Chụp bát sủi cảo nóng hổi hoặc xe mì cổ", "Photo of steaming dumpling bowl or noodle cart", "拍摄一碗热气腾腾的水饺或复古面车", "湯気立つ水餃子または麺台車の写真を撮影", "뜨끈한 만두 한 그릇 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.CURRENT
                ),
                QuestStop(
                    id = "stop_02_q5",
                    placeId = "q5_2",
                    name = l(lang, "Hội Quán Ôn Lăng & Hẻm Chùa Bà (P. Chợ Lớn)", "On Lang Guildhall & Shrine Alley (Cho Lon Ward)", "温陵会馆与天后宫古巷（堤岸坊）", "オンラン会館＆天后宮ヘム（チョロン坊）", "온랑 회관 & 천후궁 골목 (쩌롱동)"),
                    category = l(lang, "Hội Quán & Di Sản", "Heritage Guildhall", "百年华人会馆", "歴史的会館建築", "역사 회관"),
                    latitude = 10.753800,
                    longitude = 106.660100,
                    whySelected = l(lang, "Di tích kiến trúc nghệ thuật Phúc Kiến với mái ngói gốm sứ tinh xảo.", "Fujian artistic landmark featuring intricate glazed ceramic roof figurines.", "建于18世纪中叶的闽南古韵建筑，飞檐雕栋覆满精美石湾陶瓷脊饰。", "18世紀建立の福建会館。精緻な陶器細工の屋根瓦が美しい。", "18세기 중엽 건립된 복건 회관. 정교한 도자기 장식 지붕."),
                    story = l(lang, "Hương trầm vòng cuộn tròn tỏa khói huyền ảo trong không gian cổ kính thanh tịnh.", "Large incense coils hanging from temple ceilings filling the air with serene aromas.", "盘香缭绕、梵音渺渺，见证百年华人先民渡海南下奋斗的沧桑记忆。", "渦巻き線香の煙が静かに漂う、華僑の歴史と祈りが息づく空間。", "나선형 향 연기가 신비롭게 피어오르는 유서 깊은 화교 사원."),
                    factReference = "National Heritage Monument On Lang",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh nhang vòng hoặc mái gốm sứ cổ", "Photo of incense coils or ceramic roof", "拍摄悬挂的盘香或古建屋脊陶塑", "渦巻き線香または屋根の陶器細工を撮影", "나선형 향 또는 지붕 도자기 장식 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_03_q5",
                    placeId = "q5_3",
                    name = l(lang, "Hẻm Thuốc Bắc Lương Nhữ Học & Triệu Quang Phục (P. Chợ Lớn)", "Herbal Medicine Alley Luong Nhu Hoc (Cho Lon Ward)", "梁汝学传统草药中药街（堤岸坊）", "ルオンニューホック漢方薬通り（チョロン坊）", "르엉느혹 한약 골목 (쩌롱동)"),
                    category = l(lang, "Phố Thuốc Đông Y", "Traditional Medicine Alley", "百年中药街巷", "漢方薬屋街", "전통 한약 거리"),
                    latitude = 10.752500,
                    longitude = 106.658200,
                    whySelected = l(lang, "Khu phố đông y cổ truyền ngập tràn mùi hương thảo mộc và quế hồi đặc trưng.", "Historic herbalist street filled with aromas of cinnamon, star anise, and ginseng.", "整条街巷弥漫着肉桂、八角与当归等天然名贵中草药的醇厚芳香。", "シナモンや八角の香りが漂う、何代も続く老舗漢方薬局の街路。", "계피, 팔각, 당귀 향이 가득한 전통 한약방 거리."),
                    story = l(lang, "Những chiếc tủ thuốc bằng gỗ lim hàng trăm ngăn kéo cổ kính được gìn giữ qua 3-4 thế hệ.", "Centuries-old wooden herb cabinets with hundreds of brass-handled drawers.", "珍贵的实木百子柜与传承数代的中医世家切药研粉的手艺绝活。", "何百もの引き出しを持つ年代物の百草薬箪笥と熟練の薬研ぎ技術。", "수백 개의 서랍을 가진 고풍스러운 백자 약장과 약재 손질 기술."),
                    factReference = "Saigon Chinese Medicine Heritage",
                    challenge = Challenge(prompt = l(lang, "Chụp tủ thuốc đông y cổ hoặc mẹt thảo mộc", "Photo of vintage medicine cabinet or drying herbs", "拍摄古朴的中药百子柜或晾晒的草药", "漢方薬箪笥または干しハーブを撮影", "옛 약재장 또는 약초 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_04_q5",
                    placeId = "q5_4",
                    name = l(lang, "Hẻm Trà Thảo Mộc & Chè Cổ Truyền Hà Tôn Quyền (P. Chợ Lớn)", "Herbal Tea & Heritage Sweet Soup Alley (Cho Lon Ward)", "广式传统糖水与凉茶铺小巷（堤岸坊）", "伝統中華スイーツ＆涼茶路地（チョロン坊）", "전통 화교 디저트 & 허브차 골목 (쩌롱동)"),
                    category = l(lang, "Chè Hoa Cổ Truyền", "Sweet Soup Alley", "广式甜品名点", "中華スイーツ通り", "화교 디저트 골목"),
                    latitude = 10.755100,
                    longitude = 106.656500,
                    whySelected = l(lang, "Các quán chè hoa lâu đời với chè hột gà trà, chè mè đen và quy linh cao thanh mát.", "Famous sweet soup stalls serving herbal egg tea, black sesame soup, and herbal jelly.", "传承半个世纪的广式传统甜品铺，提供热芝麻糊、凤凰奶糊与龟苓膏。", "半世紀続く中華甘味処。黒胡麻汁やお茶卵デザートが絶品。", "반세기 전통의 흑임자죽, 계란 약차 등 정통 화교 디저트 맛집."),
                    story = l(lang, "Thức quà tráng miệng thanh tao làm dịu đi cái nắng phương Nam của người Sài Gòn - Chợ Lớn.", "Gentle herbal desserts that refresh the soul after a long stroll under the tropical sun.", "清甜爽口、润燥养生的古法糖水，是炎炎夏日里最惬意的市井慰藉。", "南国の暑さを忘れさせてくれる、身体に優しい伝統のヘルシースイーツ。", "남국의 무더위를 식혀주는 건강하고 달콤한 전통 화교 디저트."),
                    factReference = "Cho Lon Culinary Guide",
                    challenge = Challenge(prompt = l(lang, "Chụp bát chè mè đen hoặc chén trà thảo mộc", "Photo of sweet soup dessert or herbal tea", "拍摄一碗浓郁的黑芝麻糊或特色甜品", "デザートやハーブティーの写真を撮影", "디저트 또는 허브차 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_05_q5",
                    placeId = "q5_5",
                    name = l(lang, "Hào Sĩ Phường - Con Hẻm Chung Cư Cổ 100 Năm (P. Chợ Lớn)", "Hao Si Phuong 100-Year Historic Alley (Cho Lon Ward)", "豪士坊百年华裔老宅聚落（堤岸坊）", "ハオシーフオン 百年ヘム集落（チョロン坊）", "하오시프엉 100년 전통 화교 골목 (쩌롱동)"),
                    category = l(lang, "Hẻm Di Sản Người Hoa", "Heritage Alley Enclave", "百年华埠胡同", "歴史的ヘム集落", "백년 전통 골목"),
                    latitude = 10.751100,
                    longitude = 106.661800,
                    whySelected = l(lang, "Khu dân cư người Hoa xây dựng hơn một thế kỷ trước với ban công gỗ và câu đối đỏ.", "Centuries-old Chinese residential courtyard with distinctive shared wooden balconies.", "拥有百年历史的传统回字形两层骑楼聚落，红漆对联与斑驳绿墙相映生辉。", "100年以上の歴史を持つ2階建て長屋路地。赤い対聯と木のバルコニー。", "100년 넘는 역사를 지닌 2층 전통 화교 공동주택 골목."),
                    story = l(lang, "Không gian sống bình yên, nơi những gia đình người Hoa gắn bó qua 4-5 thế hệ.", "A peaceful pocket where generations of Cantonese and Teochew families live in harmony.", "宛如时光停滞的清幽院落，见证了几代广府与潮州家庭的淳朴邻里温情。", "時代を超えて広東・潮州の人々が穏やかに暮らす温もりある路地裏。", "광둥 및 조주 화교 가족들이 대대로 평화롭게 살아가는 따뜻한 공동체."),
                    factReference = "Hao Si Phuong Heritage Registry",
                    challenge = Challenge(prompt = l(lang, "Chụp góc hành lang gỗ hoặc câu đối đỏ", "Photo of wooden balcony or red couplets", "拍摄两层木制走廊或古朴红木门联", "木のバルコニーまたは赤い対聯を撮影", "목조 발코니 또는 붉은 춘절 글귀 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                )
            )
            "q3_bunker" -> listOf(
                QuestStop(
                    id = "stop_01_q3b",
                    placeId = "q3b_1",
                    name = l(lang, "Hầm Vũ Khí Bí Mật Biệt Động Sài Gòn (287/70 Nguyễn Đình Chiểu, P. Bàn Cờ)", "Saigon Commandos Secret Weapons Bunker (Ban Co Ward)", "西贡特工地下军火库秘密旧址（棋盘坊）", "サイゴン別動隊 秘密武器地下壕（バンコー坊）", "사이공 특공대 비밀 무기 벙커 (반꺼동)"),
                    category = l(lang, "Di Tích Lịch Sử Quốc Gia", "National Historic Site", "国家级历史遗迹", "国家歴史記念碑", "국가 역사 유적지"),
                    latitude = 10.775800,
                    longitude = 106.686100,
                    whySelected = l(lang, "Hầm chứa vũ khí bí mật lớn nhất nội đô Sài Gòn phục vụ cuộc Tổng tiến công Tết Mậu Thân 1968.", "Largest clandestine underground weapons depot in downtown Saigon for the 1968 Tet Offensive.", "1968年新春攻势期间隐藏在市中心居民区地下最大的秘密军火库。", "1968年テト攻勢で使われた市内最大の地下武器庫跡。", "1968년 구정 대공세 당시 도심 지하 최대의 비밀 무기 저장고."),
                    story = l(lang, "Nằm ngụy trang tinh vi dưới tấm ván sàn phòng khách của căn nhà hẻm bình thường.", "Ingeniously disguised beneath the wooden living room flooring of an ordinary alley house.", "伪装在普通民房木地板下方，秘密运送并储存了近两吨枪支弹药与炸药。", "普通の民家の床下に巧妙に隠され、2トンもの武器が極秘保管されていた。", "일반 주택 거실 바닥 아래 정교하게 위장된 2톤 규모의 비밀 무기 벙커."),
                    factReference = "Di tích Lịch sử Quốc gia Hầm Vũ Khí Biệt Động Sài Gòn",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh nắp hầm bí mật hoặc kỷ vật lịch sử", "Photo of secret hatch cover or vintage artifact", "拍摄隐蔽地下暗门或历史文物", "秘密の床下ハッチまたは歴史遺品を撮影", "비밀 해치 또는 역사 유물 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.CURRENT
                ),
                QuestStop(
                    id = "stop_02_q3b",
                    placeId = "q3b_2",
                    name = l(lang, "Hẻm Cà Phê Đỗ Phủ - Biệt Động Sài Gòn (P. Bàn Cờ)", "Hẻm Đỗ Phủ Ranger Alley & Safehouse (Ban Co Ward)", "杜府咖啡 - 西贡特攻深巷据点（棋盘坊）", "ドーフーカフェ（サイゴン別動隊ヘム - バンコー坊）", "도푸 카페 - 사이공 특공대 골목 (반꺼동)"),
                    category = l(lang, "Hẻm Cà Phê Cổ", "Vintage Alley Cafe", "隐秘历史咖啡馆", "秘密歴史カフェ路地", "골목 역사 카페"),
                    latitude = 10.776664,
                    longitude = 106.684156,
                    whySelected = l(lang, "Quán cà phê cổ nằm sâu trong hẻm, từng là trạm giao liên mật tại Phường Bàn Cờ.", "Retro coffee house in the alley that served as a secret courier station in Ban Co Ward.", "位于棋盘坊深藏于巷弄内部的复古咖啡馆，曾是秘密联络站。", "バンコー坊の路地奥に佇むレトロなカフェ。かつて秘密の連絡所として機能。", "반꺼동의 골목 안쪽에 숨어있는 빈티지 카페. 과거 비밀 연락소."),
                    story = l(lang, "Nơi cất giấu tài liệu và đường hầm thoát hiểm bí mật hướng ra lối hẻm Phường Bàn Cờ.", "Preserving original secret lockers and escape hatches in Ban Co Ward.", "店内保留着当年传送信件的暗格与通向暗巷的紧急撤退口（棋盘坊）。", "秘密の暗格や路地へと続く脱出路が当時のまま残されている（バンコー坊）。", "비밀 서류함과 골목으로 연결되는 탈출구가 남아있는 곳 (반꺼동)."),
                    factReference = "Saigon Rangers Historical Guild (Ban Co Ward)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh chiếc tủ gỗ chứa vách hầm", "Photo of vintage wooden cabinet", "拍摄隐藏密室通道的复古老木柜", "隠し扉のあるレトロな木製タンスを撮影", "비밀 통로가 숨겨진 빈티지 목재장 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_03_q3b",
                    placeId = "q3b_3",
                    name = l(lang, "Hẻm Tiệm Phở Bình - Sở Chỉ Huy Biệt Động (P. Bàn Cờ)", "Phở Bình Ranger Command Post Alley (Ban Co Ward)", "平米粉店巷弄 - 西贡特攻指挥所（棋盘坊）", "フォービン路地 - サイゴン別動隊司令部跡（バンコー坊）", "포빈 골목 - 사이공 특공대 지휘소 (반꺼동)"),
                    category = l(lang, "Hẻm Di Tích Lịch Sử", "Historical Command Post", "历史指挥部巷", "歴史的指令部路地", "역사 지휘소 골목"),
                    latitude = 10.778800,
                    longitude = 106.685200,
                    whySelected = l(lang, "Quán phở trong hẻm từng là sở chỉ huy chiến dịch Mậu Thân 1968 tại Phường Bàn Cờ.", "Noodle shop alley that served as the operational command headquarters in 1968 in Ban Co Ward.", "位于棋盘坊，曾作为1968年新春攻势特攻行动最高秘密指挥所的著名米粉店。", "バンコー坊の1968年テト攻勢で秘密司令部として使われた路地裏のフォー店。", "1968년 구정 대공세 당시 비밀 지휘부로 쓰였던 반꺼동의 역사적인 쌀국수집 골목."),
                    story = l(lang, "Gia đình cụ Ngô Toại che giấu các chiến sĩ biệt động ngay tầng lầu trong con hẻm Phường Bàn Cờ.", "Preserved upper floors where secret missions were planned under the guise of an alley noodle shop.", "吴遂先生一家在棋盘坊米粉店二楼密谋策划特攻行动的英勇岁月。", "バンコー坊のフォー店の2階で秘密任務の計画が練られていた当時の記憶。", "반꺼동 쌀국수집 2층에서 비밀 작전이 모의되던 영웅적인 역사의 현장."),
                    factReference = "Di tích Quốc gia Sở Chỉ Huy Biệt Động (Phở Bình)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh tấm biển di tích hoặc tô phở truyền thống", "Photo of heritage plaque or traditional pho", "拍摄国家历史遗迹铭牌或热气腾腾的传统米粉", "歴史記念プレートまたは伝統のフォーの写真を撮影", "국가 역사 유적 명판 또는 전통 쌀국수 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_04_q3b",
                    placeId = "q3b_4",
                    name = l(lang, "Hẻm Bàn Cờ - Mê Cung Ngõ Ngách Sài Gòn (P. Bàn Cờ)", "Bàn Cờ Chessboard Alley Network (Ban Co Ward)", "棋盘街区 - 西贡经典迷宫胡同网络（棋盘坊）", "バンコー迷宮路地ネットワーク（バンコー坊）", "반꺼 체스판 골목 미로 (반꺼동)"),
                    category = l(lang, "Mê Cung Hẻm Phố", "Chessboard Alley Network", "棋盘街区巷弄", "迷宮路地ネットワーク", "체스판 골목길"),
                    latitude = 10.772500,
                    longitude = 106.681500,
                    whySelected = l(lang, "Cấu trúc hẻm bàn cờ độc nhất vô nhị giúp các chiến sĩ ngụy trang và di chuyển bí mật.", "Unique chessboard grid system of interconnected alleys enabling swift clandestine movements.", "西贡独一无二的棋盘状互通小巷格局，曾是地下交通员隐蔽转运的天然屏障。", "サイゴン唯一の碁盤の目状の路地構造。秘密の移動や隠れ家に最適だった。", "사이공 유일의 격자형 체스판 골목 구조로 비밀 이동에 완벽했던 미로."),
                    story = l(lang, "Mỗi ngã rẽ đều mở ra một quán ăn gia truyền, gánh chè ngọt và nhịp sống xóm giềng nồng ấm.", "Every corner turns into family-run food nooks, sweet dessert pots, and friendly neighborhood life.", "每个转角都藏着世代相传的小吃铺、老牌甜品摊与温暖亲切的邻里日常。", "角を曲がるたびに秘伝の食堂や甘味処が現れる、温かい下町コミュニティ。", "골목 모퉁이마다 대를 이은 노포 맛집과 정겨운 이웃들의 삶이 펼쳐지는 곳."),
                    factReference = "Ban Co Neighborhood Heritage",
                    challenge = Challenge(prompt = l(lang, "Chụp ngã ba hẻm bàn cờ hoặc quán ăn gia truyền", "Photo of chessboard alley intersection or family eatery", "拍摄棋盘巷交叉口或老字号特色餐馆", "碁盤の目の路地交差点または老舗食堂を撮影", "골목 교차로 또는 오래된 노포 식당 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_05_q3b",
                    placeId = "q3b_5",
                    name = l(lang, "Hẻm Chợ Vườn Chuối & Trà Đá Vỉa Hè (P. Bàn Cờ)", "Vườn Chuối Market Alley & Iced Tea Corner (Ban Co Ward)", "香蕉园市集深巷与路边冰茶摊（棋盘坊）", "ヴオンチュオイ市場路地＆アイスティー（バンコー坊）", "브온추오이 시장 골목 & 길거리 아이스티 (반꺼동)"),
                    category = l(lang, "Ẩm Thực & Chợ Hẻm", "Market Alley & Street Food", "市井集市与风味小吃", "市場路地＆屋台", "골목 시장 & 길거리 음식"),
                    latitude = 10.774100,
                    longitude = 106.683200,
                    whySelected = l(lang, "Chợ hẻm trăm tuổi tấp nập với những món ăn đậm hồn Sài Gòn xưa.", "Century-old market alley bustling with authentic old Saigon street delicacies.", "百年历史的传统深巷小菜场，荟萃了无数纯正地道的老西贡风味美食。", "100年の歴史を持つ路地裏市場。サイゴンの昔ながらの味が勢揃い。", "100년 역사의 골목 시장. 옛 사이공의 소박한 전통 맛이 가득한 곳."),
                    story = l(lang, "Ngồi nhâm nhi ly trà đá mát lạnh bên mâm bánh bèo, bánh tằm bì và lắng nghe chuyện xưa.", "Sipping refreshing iced tea alongside steamed rice cakes while listening to historical tales.", "品尝一杯冰凉沁心的茉莉香冰茶，配一份软糯香浓的传统糕点，回味峥嵘岁月。", "冷たいジャスミン茶と伝統の米粉スイーツを味わいながら歴史に思いを馳せる。", "시원한 아이스티와 쫄깃한 떡 디저트를 즐기며 옛이야기를 나누는 정겨운 시간."),
                    factReference = "Vuon Chuoi Historic Market Guild",
                    challenge = Challenge(prompt = l(lang, "Chụp ly trà đá hoặc sạp bánh dân gian trong hẻm", "Photo of iced tea glass or traditional cake stall", "拍摄一杯路边冰茶或传统特色米糕摊", "アイスティーまたは伝統菓子の屋台を撮影", "아이스티 잔 또는 전통 떡 가판대 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                )
            )
            "q10_bk_food" -> listOf(
                QuestStop(
                    id = "stop_01_bkf",
                    placeId = "bkf_1",
                    name = l(lang, "Hẻm 493 Tô Hiến Thành - Bánh Tráng Nướng & Bò Bía (P. Diên Hồng)", "493 To Hien Thanh - Grilled Rice Paper Alley (Dien Hong)", "苏宪成493号巷烤米纸与薄饼（延洪坊）", "トーヒエンタイン493番地ヘム＆ライスペーパー焼き（ディエンホン坊）", "토히엔탄 493번지 라이스페이퍼 구이 & 보비아 (디엔홍동)"),
                    category = l(lang, "Hẻm Ăn Vặt Sinh Viên", "Student Street Snacks", "学生街头小吃", "学生ストリートフード", "대학가 길거리 간식"),
                    latitude = 10.774800,
                    longitude = 106.662200,
                    whySelected = l(lang, "Thủ phủ ăn vặt sinh viên Bách Khoa với món bánh tráng nướng trứng cút và bò bía ngọt.", "The ultimate BK student snacking spot famous for sizzling grilled rice paper and spring rolls.", "理工大学最负盛名的学生小吃聚集地，以现烤鹌鹑蛋米纸与甜薄饼闻名。", "香ばしいウズラ卵のライスペーパー焼きや生春巻きが人気の工科大生御用達の路地。", "공대생들이 즐겨 찾는 바삭한 라이스페이퍼 구이와 롤 스낵 명소."),
                    story = l(lang, "Mỗi chiều tan học, con hẻm rực rỡ ánh than hồng và rộn rã tiếng cười của các tân kỹ sư.", "Every afternoon after class, glowing charcoal braziers and joyful student chatter fill the alley.", "放学后炭火红光点点，洋溢着莘莘学子们的欢声笑语与扑鼻香气。", "放課後になると炭火の赤が灯り、学生たちの賑やかな笑い声に包まれる。", "방과 후 숯불 향과 함께 공대생들의 웃음소리가 가득 울려 퍼지는 활기찬 골목."),
                    factReference = "Saigon Student Street Food Archives",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh bếp than nướng bánh tráng", "Photo of charcoal grill for rice paper", "拍摄烤米纸的炭火炉", "ライスペーパーを焼く炭火台を撮影", "라이스페이퍼 굽는 숯불 화로 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.CURRENT
                ),
                QuestStop(
                    id = "stop_02_bkf",
                    placeId = "bkf_2",
                    name = l(lang, "Quán Cơm Tấm Đồ Án Tăng Ca (Cổng 3 Tô Hiến Thành - P. Diên Hồng)", "All-Nighter Broken Rice (Gate 3 To Hien Thanh)", "通宵答辩碎米饭老店（苏宪成三号门）", "深夜残業コムタム（3号門トーヒエンタイン）", "야근 과제 껌땀 맛집 (3번 게이트 토히엔탄)"),
                    category = l(lang, "Cơm Tấm Sinh Viên", "Student Broken Rice", "学生碎米饭", "学生コムタム", "학생 껌땀"),
                    latitude = 10.773500,
                    longitude = 106.660800,
                    whySelected = l(lang, "Quán cơm mở đến nửa đêm với truyền thống sinh viên bao no, tiếp sức mùa đồ án.", "Midnight broken rice joint offering free rice refills that fuels thesis all-nighters.", "营业至深夜的碎米饭老铺，免费加饭，是答辩季学子们的深夜食堂。", "深夜まで営業しご飯おかわり自由。卒業制作を支え続ける学生の味方。", "무료 밥 리필로 졸업 프로젝트 학생들의 든든한 밤샘을 책임지는 노포."),
                    story = l(lang, "Đĩa sườn nướng mỡ hành đậm đà cùng chén canh khổ qua dồn thịt cứu cánh bao đêm thức trắng.", "Scallion oil pork chops paired with stuffed bitter melon soup powering engineers through midnight sprints.", "葱油炭烤猪排搭配酿肉苦瓜热汤，温暖抚慰了无数熬夜画图的疲惫身心。", "ネギ油が香るポークチョップと温かいスープが徹夜の疲れを癒やす。", "파기름 돼지갈비와 따뜻한 국물이 밤샘 공부의 피로를 씻어주는 곳."),
                    factReference = "BK Culinary Lore (Dien Hong Ward)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh đĩa cơm sườn trứng ốp la", "Photo of broken rice plate with egg", "拍摄一份带荷包蛋的碎米饭", "目玉焼き付きコムタムの写真を撮影", "계란 후라이가 올라간 껌땀 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_03_bkf",
                    placeId = "bkf_3",
                    name = l(lang, "Hẻm 284 Lý Thường Kiệt & Trà Sữa Tươi Thanh Trùng (P.14, Q.10)", "284 Ly Thuong Kiet Alley & Fresh Milk Tea (Ward 14)", "李常杰284号巷与鲜奶茶铺（14坊）", "リトゥオンキエット284番地ヘム＆フレッシュミルクティー（14坊）", "리트엉끼엣 284번지 골목 & 프레시 밀크티 (14동)"),
                    category = l(lang, "Giải Khát Sinh Viên", "Student Milk Tea", "学生奶茶饮品", "学生カフェ＆ティー", "학생 밀크티 & 음료"),
                    latitude = 10.770500,
                    longitude = 106.657200,
                    whySelected = l(lang, "Con hẻm rợp bóng cây tập trung các tiệm trà sữa và nước mía sầu riêng mát lạnh.", "Shaded alley hosting iconic student fresh milk tea and durian sugarcane juice stands.", "绿树成荫的幽静小巷，汇聚了平价手作鲜奶茶与特色榴莲甘蔗汁。", "手作りミルクティーやドリアンサトウキビジュースが楽しめる憩いの緑陰路地。", "수제 밀크티와 시원한 사탕수수 주스를 맛볼 수 있는 그늘진 쉼터 골목."),
                    story = l(lang, "Điểm dừng chân lý tưởng để sinh viên bàn luận đề tài tốt nghiệp sau những giờ thí nghiệm.", "The go-to pitstop for engineering project syncs and debriefs after long lab sessions.", "实验课后学子们围坐一堂、研讨学术课题与畅想未来的惬意落脚点。", "実験や講義の後に学生たちが集まり、卒業研究を語り合う憩いの場所。", "실험과 수업을 마친 학생들이 모여 담소를 나누는 여유로운 휴식처."),
                    factReference = "Saigon Youth Alley Culture",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh ly trà sữa hoặc xe nước mía hẻm", "Photo of milk tea glass or sugarcane cart", "拍摄一杯冰凉奶茶或甘蔗汁车", "ミルクティーまたはサトウキビ屋台を撮影", "밀크티 잔 또는 사탕수수 카트 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                )
            )
            "q4_riverfront" -> listOf(
                QuestStop(
                    id = "stop_01_q4",
                    placeId = "q4_1",
                    name = l(lang, "Cầu Mống Di Sản 1893 & Lối Đi Bộ Bờ Kênh Bến Nghé", "1893 Historic Cầu Mống Bridge & Canal Promenade", "1893年百年彩虹古桥与滨河步道", "1893年架橋コーモン橋＆運河プロムナード", "1893년 건립 꺼우몽 역사 보행교 & 운하 산책로"),
                    category = l(lang, "Di Tích Lịch Sử", "Historic Bridge", "历史建筑遗迹", "歴史的建造物", "역사 유적지"),
                    latitude = 10.769800,
                    longitude = 106.704200,
                    whySelected = l(lang, "Cây cầu bộ hành bằng thép cổ kính bắc qua kênh Bến Nghé nối liền trung tâm với khu xóm cũ.", "Historic green iron pedestrian bridge spanning Ben Nghe Canal connecting downtown to historic riverside alleys.", "连接市中心与历史水岸街区、横跨奔艺运河的百年墨绿色铁制步行古桥。", "サイゴン中心部と川沿いの古い街路を結ぶ、歴史ある緑の鉄製歩行者橋。", "사이공 도심과 강변 구시가지를 잇는 1893년 건립 클래식 녹색 철교."),
                    story = l(lang, "Do công ty Eiffel thiết kế xây dựng từ thế kỷ 19, ngắm toàn cảnh sông nước và tòa nhà cổ.", "Designed by the Eiffel company in the late 19th century, offering breezy vistas of riverboats and historic shorelines.", "由法国埃菲尔工程公司于19世纪末设计建造，饱览往来驳船与沿岸百年老建筑风光。", "19世紀末エッフェル社設計。川を行き交う船と対岸の歴史的建造物を一望できる。", "19세기 말 에펠사에서 설계한 역사적인 다리로 시원한 강바람과 옛 건축물 조망."),
                    factReference = "Di tích Kiến trúc Cầu Mống 1893",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh vòm thép cầu Mống màu xanh cổ", "Photo of vintage green iron arch", "拍摄绿色铁桥标志性钢架拱形结构", "緑のアーチ鉄橋の写真を撮影", "녹색 철제 아치 다리 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.CURRENT
                ),
                QuestStop(
                    id = "stop_02_q4",
                    placeId = "q4_2",
                    name = l(lang, "Hẻm Cư Xá Vĩnh Hội & Ban Công Hoa Giấy (Đường Bến Vân Đồn)", "Vĩnh Hội Heritage Apartments Alley & Bougainvillea", "永会社区老建筑巷弄与三角梅阳台", "ビンホイ旧集合住宅路地＆ブーゲンビリア", "빈호이 옛 아파트 골목 & 부겐빌레아 발코니"),
                    category = l(lang, "Chung Cư Di Sản", "Heritage Quarter", "水岸经典老楼", "レトロ公営集合住宅", "헤리티지 주거 골목"),
                    latitude = 10.766500,
                    longitude = 106.702500,
                    whySelected = l(lang, "Khu cư xá ven kênh với kiến trúc hành lang mở và giàn hoa giấy rủ bóng xuống con hẻm.", "Riverside residential block with open-air corridors and cascades of bright bougainvillea.", "坐落于运河畔的开敞式长廊老住宅楼，垂落着艳丽的三角梅花瀑。", "開放的な外廊下と色鮮やかなブーゲンビリアが映える運河沿いのレトロアパート。", "시원한 복도와 화사한 부겐빌레아 꽃이 골목을 감싸는 운하변 빈티지 아파트."),
                    story = l(lang, "Nơi từng là thương cảng sầm uất với các kho gạo, nhà máy xay xát thế kỷ trước nay thành chốn bình yên.", "Once a roaring rice-trading port lined with brick warehouses, now a serene and friendly neighborhood enclave.", "昔日米仓码头林立的繁忙商埠，如今已蜕变为充满温情与静谧的市井生活绿洲。", "かつて米問屋やレンガ造りの倉庫が並んだ港町が、穏やかな下町へと変遷。", "과거 쌀 물류 창고가 모여있던 번화한 항구에서 고즈넉하고 따뜻한 골목길로 변모."),
                    factReference = "Ben Van Don Historic Port Guild",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh giàn hoa giấy trên tường chung cư", "Photo of bougainvillea against vintage wall", "拍摄老楼外墙上的三角梅花架", "アパートの壁に咲くブーゲンビリアを撮影", "옛 아파트 벽면 부겐빌레아 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_03_q4",
                    placeId = "q4_3",
                    name = l(lang, "Hẻm Ốc Đêm Tôn Đản & Nước Mía Cốt Dừa", "Tôn Đản Night Seafood Alley & Coconut Sugarcane", "尊诞夜市炒螺与椰浆甘蔗汁", "トンダン深夜貝料理ヘム＆ココナッツサトウキビ", "똔단 심야 해산물 골목 & 코코넛 사탕수수"),
                    category = l(lang, "Ẩm Thực Đêm Bờ Sông", "Late-Night Seafood", "水岸深夜美食", "下町ナイトグルメ", "심야 해산물 먹거리"),
                    latitude = 10.764100,
                    longitude = 106.705800,
                    whySelected = l(lang, "Con hẻm ẩm thực đêm trứ danh phương Nam với hàng chục món ốc chế biến đậm đà.", "Legendary Southern night dining alley renowned for dozens of flavorful seafood dishes.", "西贡赫赫有名的深夜海鲜小吃街，汇集了数十种香浓地道的炒螺风味。", "何十種類もの貝料理が手頃に味わえる、サイゴン屈指のナイトグルメ路地。", "수십 가지 조개 요리와 해산물을 맛볼 수 있는 유명한 야간 먹거리 골목."),
                    story = l(lang, "Mùi sả ớt, bơ tỏi quyện cùng gió sông thổi vào mang lại trải nghiệm ẩm thực đường phố đỉnh cao.", "Aromas of lemongrass, chili, and garlic butter carried on river breezes define street life.", "香茅、辣椒与蒜香在江风吹拂下四溢，展现出西贡最地道的市井烟火气。", "レモングラスとガーリックの香りが川風に乗って漂う活気あふれる屋台街。", "레몬그라스와 마늘 향이 강바람을 타고 퍼지는 최고의 로컬 푸드 경험."),
                    factReference = "District 4 Culinary Archives",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh nồi ốc hấp sả bốc khói", "Photo of steaming lemongrass seafood pot", "拍摄热气腾腾的香茅蒸螺锅", "湯気立つ貝料理の鍋を撮影", "김이 모락모락 나는 해산물 요리 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                )
            )
            "q5_herbal" -> listOf(
                QuestStop(
                    id = "stop_01_q5h",
                    placeId = "q5h_1",
                    name = l(lang, "Hào Sĩ Phường - Cụm Nhà Cổ 100 Năm (206 Trần Hưng Đạo, P. Chợ Lớn)", "Hào Sĩ Phường 100-Year Historic Courtyard (Cho Lon)", "豪士坊百年华裔骑楼院落（陈兴道206号）", "ハオシーフオン 百年中華回廊住宅（チャンフンダオ206番地）", "하오시프엉 100년 전통 화교 회랑 주택 (쩐흥다오 206번지)"),
                    category = l(lang, "Di Sản Kiến Trúc", "Architectural Heritage", "百年历史民居", "歴史的建築遺産", "역사 건축 유산"),
                    latitude = 10.751200,
                    longitude = 106.661900,
                    whySelected = l(lang, "Khu dân cư người Hoa xây dựng đầu thế kỷ 20 với ban công gỗ mở và câu đối đỏ.", "Early 20th-century Chinese residential enclave with open wooden balconies and red couplets.", "建于20世纪初的回字形传统华侨民居，红木扶梯与斑驳绿墙沉淀着百年光阴。", "20世紀初頭に建てられた中華風回廊住宅。木のバルコニーと赤い対聯が印象的。", "20세기 초 건립된 전통 화교 공동주택으로 목조 발코니와 붉은 춘련이 인상적인 곳."),
                    story = l(lang, "Không gian tĩnh mịch tách biệt hoàn toàn khỏi phố xá ồn ào bên ngoài, nơi thời gian ngưng đọng.", "A tranquil haven shielded from city bustle where centuries-old family traditions thrive.", "宛如时光定格的幽静小巷，四五代华人家庭在此守望相助、和睦共处。", "都会の喧騒から隔絶された静寂な空間。代々受け継がれてきた温かい暮らし。", "도심의 소음에서 벗어나 시간이 멈춘 듯 평화로운 전통 커뮤니티."),
                    factReference = "Hao Si Phuong Heritage Registry",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh hàng hiên gỗ tầng 2 hoặc biển số nhà cổ", "Photo of 2nd floor wooden balcony or vintage address plaque", "拍摄二楼木制走廊或古朴门牌", "2階の木製バルコニーまたは古い表札を撮影", "2층 목조 발코니 또는 빈티지 문패 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.CURRENT
                ),
                QuestStop(
                    id = "stop_02_q5h",
                    placeId = "q5h_2",
                    name = l(lang, "Phố Đông Y Lương Nhữ Học & Triệu Quang Phục (P. Chợ Lớn)", "Lương Nhữ Học Traditional Herbal Medicine Row (Cho Lon)", "梁汝学与赵光复传统中药百草街（堤岸坊）", "ルオンニューホック漢方薬通り（チョロン坊）", "르엉느혹 & 찌에우꽝푹 전통 한약방 거리 (쩌롱동)"),
                    category = l(lang, "Phố Thuốc Cổ Truyền", "Herbal Medicine Row", "百年中草药街", "老舗漢方薬街", "전통 한약재 거리"),
                    latitude = 10.752600,
                    longitude = 106.658300,
                    whySelected = l(lang, "Tuyến phố đông y cổ kính với mùi thảo dược quế hồi ngào ngạt và tủ thuốc trăm ngăn.", "Ancient traditional apothecary quarter filled with cinnamon scents and multi-drawer herb cabinets.", "弥漫着肉桂与沉香芬芳的古老中药商圈，陈列着百年紫檀百子药柜。", "シナモンや薬草の香りが漂う漢方薬街。無数の引き出しを持つ薬箪笥が並ぶ。", "계피와 감초 향이 가득한 유서 깊은 한약방 거리. 수백 개의 서랍을 가진 백자 약장."),
                    story = l(lang, "Các danh y thế gia bắt mạch, kê đơn và cân thuốc bằng chiếc cân tiểu ly bằng đồng cổ kính.", "Generations of herbal doctors diagnosing, prescribing, and balancing herbs on brass scales.", "中医世家代代相传把脉问诊、用古朴铜秤精准配制养生草药。", "熟練の漢方医が真鍮の天秤を使って正確に生薬を調合する伝統の技。", "대대로 이어진 한의사들이 황동 저울로 정성껏 약재를 조제하는 전통의 현장."),
                    factReference = "Cho Lon Herbal Guild Heritage",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh tủ thuốc đông y gỗ hoặc mẹt phơi quế", "Photo of wooden herb cabinet or cinnamon drying tray", "拍摄实木百子药柜或晾晒的肉桂药材", "木製薬箪笥または天日干しのハーブを撮影", "원목 약재장 또는 햇볕에 말리는 계피 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_03_q5h",
                    placeId = "q5h_3",
                    name = l(lang, "Tiệm Trà Thảo Mộc & Chè Quy Linh Cao Hà Tôn Quyền (P. Chợ Lớn)", "Hà Tôn Quyền Herbal Tea & Gui Ling Gao (Cho Lon)", "何孙权草本凉茶与古法龟苓膏老铺（堤岸坊）", "ハトンクエン伝統ハーブ涼茶＆亀ゼリー（チョロン坊）", "하똔꾸엔 전통 허브차 & 귀령고 디저트 (쩌롱동)"),
                    category = l(lang, "Dưỡng Sinh & Ẩm Thực", "Herbal Tea & Dessert", "传统养生茶饮", "薬膳スイーツ＆ハーブ茶", "전통 약선 디저트 & 한방차"),
                    latitude = 10.755400,
                    longitude = 106.656200,
                    whySelected = l(lang, "Quán trà thảo mộc gia truyền giúp thanh nhiệt cơ thể với món quy linh cao và trà hoa cúc.", "Century-old family herbal tea shop serving refreshing Chrysanthemum tea and Gui Ling Gao jelly.", "传承百年的古法凉茶与龟苓膏老店，清热解暑、生津止渴。", "菊花茶や自家製亀ゼリーで身体を整える、創業百年の伝統茶房。", "국화차와 수제 귀령고 젤리로 무더위를 식혀주는 백년 전통의 한방 디저트 전문점."),
                    story = l(lang, "Bí quyết chưng cất thảo dược từ đời ông cố truyền lại, thức uống giải nhiệt không thể thiếu của người Chợ Lớn.", "Secret herbal brewing recipes passed down across four generations, soothing the tropical heat.", "四代相传的秘方慢火熬煮，是堤岸华人历久弥新的夏日养生智慧。", "4世代にわたり受け継がれてきた秘伝の煮出し製法。南国の知恵が息づく一杯。", "4대에 걸쳐 전수된 비법 약초 달임차로 사이공 남국의 무더위를 달래주는 지혜."),
                    factReference = "Cho Lon Wellness Guide",
                    challenge = Challenge(prompt = l(lang, "Chụp chén quy linh cao hoặc bình trà thảo dược", "Photo of herbal jelly bowl or tea pot", "拍摄一碗晶莹的龟苓膏或特色凉茶壶", "亀ゼリーの小鉢またはハーブティーポットを撮影", "귀령고 젤리 또는 한방 차 주전자 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                )
            )
            else -> listOf(
                QuestStop(
                    id = "stop_01_q1",
                    placeId = "ChIJ3b5_gXkudTERy0S3B6l4L2M",
                    name = l(lang, "Hẻm 158 Pasteur & Cà Phê Vợt Xưa (P. Sài Gòn)", "158 Pasteur Alley & Net Coffee (Saigon Ward)", "巴斯德158号古巷与网滤咖啡（西贡坊）", "パストゥール158番地ヘム＆伝統ネル珈琲（サイゴン坊）", "파스퇴르 158번지 골목 & 전통 드립 커피 (사이공동)"),
                    category = l(lang, "Hẻm Cà Phê Cổ", "Net Coffee Alley", "胡同传统咖啡", "伝統珈琲路地", "전통 커피 골목"),
                    latitude = 10.778361,
                    longitude = 106.698967,
                    whySelected = l(lang, "Hẻm sâu tĩnh lặng lưu giữ hương vị cà phê vợt truyền thống tại Phường Sài Gòn.", "Peaceful residential alley preserving net-filter coffee heritage in Saigon Ward.", "位于西贡坊繁华市中心一片幽静清雅、咖啡飘香的历史小巷。", "サイゴン坊の喧騒から離れた静寂と伝統の珈琲香る路地。", "사이공동 도심 한복판의 고요하고 정겨운 전통 커피 골목."),
                    story = l(lang, "Lưu giữ mái gạch cổ và tiếng pha cà phê vợt lách cách hơn nửa thế kỷ tại Phường Sài Gòn.", "Preserves traditional tilework and half a century of net-brewed coffee sound in Saigon Ward.", "保留着传统红瓦屋顶与半个多世纪拉网滤布萃取咖啡的独特声响（西贡坊）。", "サイゴン坊にて昔ながらの瓦屋根と半世紀続くネルドリップの伝統珈琲文化。", "새로운 사이공동 중심부에서 반세기 넘게 이어온 넷 커피 문화."),
                    factReference = "Di sản Phường Sài Gòn",
                    challenge = Challenge(prompt = l(lang, "Chụp góc hẻm có mảng tường rêu cổ", "Photo of mossy alley wall", "拍摄带有青苔古墙与斑驳木门的照片", "レトロな壁や路地の入口を撮影", "이끼 낀 옛 골목 벽면 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.CURRENT
                ),
                QuestStop(
                    id = "stop_02_q1",
                    placeId = "ChIJ_tonthatdam_q1",
                    name = l(lang, "Hẻm 14 Tôn Thất Đạm - Chợ Cũ (P. Sài Gòn)", "14 Ton That Dam Alley - Old Market (Saigon Ward)", "尊室淡14号老集市古巷（西贡坊）", "トンタッダム14番地（サイゴン坊・オールドマーケット路地）", "톤땃담 14번지 골목 (사이공동 구 시장)"),
                    category = l(lang, "Hẻm Chợ Cổ", "Old Market Alley", "老集市与怀旧巷弄", "レトロ市場＆アパート路地", "옛 시장 및 빈티지 골목"),
                    latitude = 10.772200,
                    longitude = 106.704100,
                    whySelected = l(lang, "Con hẻm chợ cũ lâu đời nhất khu trung tâm Phường Sài Gòn.", "One of the oldest market alleys in downtown Saigon Ward.", "位于西贡坊最古老、充满市井烟火气的老集市胡同。", "サイゴン坊で最も古い下町市場の雰囲気が残る路地。", "사이공동 도심에서 가장 오래된 전통 시장 골목."),
                    story = l(lang, "Nơi các bạn trẻ hồi sinh chung cư cũ thành không gian cà phê nghệ thuật tại Phường Sài Gòn.", "Where young creators transformed vintage flats into creative spots in Saigon Ward.", "年轻人在西贡坊百年前的老公寓里开辟出充满惊奇的创客空间。", "サイゴン坊で若いクリエイターがレトロなアパートをアート空間に再生。", "사이공동 청년 창작자들이 고택 아파트를 아지트로 재탄생시킨 곳."),
                    factReference = "Saigon Ward Old Market Archives",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh cầu thang gạch hoa cổ", "Photo of vintage tiled staircase", "拍摄公寓内具有百年历史的花砖楼梯", "レトロな花柄タイルの階段を撮影", "클래식 타일 계단 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_03_q1",
                    placeId = "ChIJ_18a_ntmk_q1",
                    name = l(lang, "Hẻm 18A Nguyễn Thị Minh Khai (P. Tân Định)", "18A Nguyễn Thị Minh Khai Street Art Alley (Tan Dinh Ward)", "阮氏明开18A号壁画与美食巷（新定坊）", "グエンティミンカイ18A番地 アート路地（タンディン坊）", "응우옌티민카이 18A번지 스트리트 아트 골목 (떤딘동)"),
                    category = l(lang, "Hẻm Bích Họa", "Street Art Alley", "艺术壁画巷弄", "ウォールアート路地", "벽화 아트 골목"),
                    latitude = 10.785100,
                    longitude = 106.698200,
                    whySelected = l(lang, "Con hẻm sôi động nổi tiếng với tranh bích họa đường phố tại Phường Tân Định.", "Vibrant alley famous for mural street art in Tan Dinh Ward.", "位于新定坊以色彩斑斓的街头壁画闻名的活力胡同。", "タンディン坊の鮮やかなウォールアート路地。", "떤딘동의 화려한 벽화와 현지인 맛집 골목."),
                    story = l(lang, "Sự kết hợp giữa nhịp sống học sinh, sinh viên và nét vẽ đầy tự do tại Phường Tân Định.", "Connecting youth culture, student vibes, and mural art in Tan Dinh Ward.", "新定坊融合了青春学子的欢声笑语与街头艺术家的自由壁画创作。", "タンディン坊の学生街の活気とアーティストたちの自由な感性が交差する場所。", "떤딘동 젊음의 열기와 예술가들의 감성이 만나는 골목."),
                    factReference = "Urban Art Project (Tan Dinh Ward)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh một bức bích họa hẻm", "Photo of a vibrant alley mural", "拍摄一张色彩鲜艳的巷弄墙壁画", "ウォールアートの写真を撮影", "골목 벽화 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_04_q1",
                    placeId = "ChIJ_59_lyturong_q1",
                    name = l(lang, "Hẻm 59 Lý Tự Trọng & Căn Hộ Cổ (P. Sài Gòn)", "59 Lý Tự Trọng Vintage Alley (Sai Gon Ward)", "李自重59号古法式楼宇巷弄（西贡坊）", "リトゥチョン59番地 ヘム＆レトロアパート（サイゴン坊）", "리뜨쫑 59번지 클래식 아파트 골목 (사이공동)"),
                    category = l(lang, "Hẻm Kiến Trúc Cổ", "Colonial Heritage Alley", "古典建筑巷弄", "歴史的コロニアル路地", "클래식 건축 골목"),
                    latitude = 10.776500,
                    longitude = 106.699100,
                    whySelected = l(lang, "Hẻm nhỏ uốn lượn cạnh chung cư gạch cổ rợp bóng cây tại Phường Sài Gòn.", "Winding alley adjacent to shaded vintage brick buildings in Sai Gon Ward.", "位于西贡坊曲径通幽，倚靠着古朴老砖墙公寓的经典胡同。", "サイゴン坊のレトロなレンガ造りの建物の脇をすり抜ける緑豊かな曲がり路地。", "사이공동 빈티지 벽돌 건물 옆으로 굽이굽이 이어지는 푸른 골목."),
                    story = l(lang, "Nơi ẩn chứa những phòng tranh, tiệm gốm nhỏ và quán cà phê ban công tại Phường Sài Gòn.", "Hiding independent art galleries, ceramic workshops, and balcony cafes in Sai Gon Ward.", "西贡坊隐匿着精品画廊、温润陶艺作坊与阳台咖啡观景台。", "サイゴン坊の小さなギャラリーや陶芸工房、バルコニーカフェが隠れている。", "사이공동 작은 갤러리와 도자기 공방, 발코니 카페가 숨어있는 곳."),
                    factReference = "Heritage Index (Sai Gon Ward)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh mảng tường gạch rêu hẻm", "Photo of vintage brick alley wall", "拍摄复古红砖墙面与红瓦屋檐的搭配", "ノスタルジックなレンガ壁の写真を撮影", "빈티지 벽돌 벽면 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                ),
                QuestStop(
                    id = "stop_05_q1",
                    placeId = "ChIJ_212_nguyentrai_q1",
                    name = l(lang, "Hẻm 212 Nguyễn Trãi & Cơm Tấm (P. Cầu Ông Lãnh)", "212 Nguyễn Trãi Broken Rice Alley (Cau Ong Lanh Ward)", "阮廌212号碎米饭美食胡同（考翁领坊）", "グエンチャイ212番地 碎米飯路地（カウオンラン坊）", "응우옌짜이 212번지 쩜똠 골목 (꺼우옹라인동)"),
                    category = l(lang, "Hẻm Ẩm Thực", "Culinary Corridor Alley", "市井美食巷", "下町グルメ路地", "골목 먹거리 체인"),
                    latitude = 10.768900,
                    longitude = 106.688500,
                    whySelected = l(lang, "Con hẻm sầm uất với mùi sườn nướng thơm nức tại Phường Cầu Ông Lãnh.", "Bustling foodie alley filled with grilled pork rib aromas in Cau Ong Lanh Ward.", "位于考翁领坊整条胡同扑鼻而来碳烤排骨美味。", "カウオンラン坊の炭火で焼く香ばしい豚肋肉の香りが漂うグルメ路地。", "꺼우옹라인동 숯불 돼지고기 구이 향이 가득한 먹거리 명소."),
                    story = l(lang, "Bữa ăn cơm tấm bình dân gắn liền với ký ức bao thế hệ tại Phường Cầu Ông Lãnh.", "Broken rice meals deep rooted in daily memories in Cau Ong Lanh Ward.", "考翁领坊碎米饭是深植于几代西贡人日常生活中最温暖的市井美味。", "カウオンラン坊で世代を超えて爱され続けるサイゴンのソウルフード。", "꺼우옹라인동 수세대에 걸쳐 사랑받아 온 사이공의 영혼의 음식 쩜똠."),
                    factReference = "Saigon Culinary Heritage Register (Cau Ong Lanh Ward)",
                    challenge = Challenge(prompt = l(lang, "Chụp ảnh làn khói sườn nướng trong hẻm", "Photo of smoking grill in alley", "拍摄巷弄里炭火烤肉烟雾缭绕的市井画面", "炭火焼きの煙が上がる雰囲気を撮影", "숯불 연기가 노란 골목 맛집 사진 촬영"), type = "PHOTO_OR_SKIP"),
                    status = StopStatus.UPCOMING
                )
            )
        }

        val title = when (questType) {
            "q_thanhda" -> l(lang, "Ký Ức Thanh Đa: Hẻm Bờ Sông & Cư Xá Cũ", "Thanh Đa Memories: Riverside Alleys & Heritage Apartments", "青多记忆：滨江深巷与复古老楼", "タインダーの記憶：リバーサイド路地とレトロ団地", "탄다의 추억: 강변 골목과 레트로 아파트")
            "q10_bk" -> l(lang, "Bách Khoa Sài Gòn & Hẻm Sinh Viên", "HCMUT Bách Khoa & Student Alleyways", "胡志明市理工大学与青春学生巷弄", "ホーチミン工科大学（BK）＆学生街のヘム", "호치민 공과대학(BK) & 대학가 청춘 골목")
            "q10_bk_food" -> l(lang, "Bách Khoa Ẩm Thực Sinh Viên & Hẻm Đêm Đồ Án", "BK Student Food Trail & All-Nighter Alleys", "理工大学学生美食与挑灯夜战寻味之旅", "工科大グルメ探訪＆深夜のプロジェクト路地", "BK 대학가 먹거리 탐방 & 밤샘 과제 골목")
            "q11_crafts" -> l(lang, "Làng Lồng Đèn Phú Bình & Xưởng Thủ Công", "Phú Bình Lantern Village & Local Crafts", "富平灯笼传统手艺村", "フービン ランタン作りと伝統工芸街", "푸빈 등불 전통 공예 마을")
            "q3_french" -> l(lang, "Hẻm Biệt Thự Cổ & Cà Phê Nắng Sớm", "French Colonial Villa & Morning Coffee Alley", "法式古墅与优雅庭院咖啡小巷", "フレンチコロニアル洋館＆朝の珈琲路地", "프렌치 빌라 & 아침 커피 골목")
            "q5_food" -> l(lang, "Hẻm Ẩm Thực & Hội Quán Chợ Lớn", "Chợ Lớn Heritage Food Alley", "堤岸百年美食与会馆寻味之旅", "チョロン 屋台グルメ＆レトロ歴史路地", "쩌롱 먹거리 골목 & 역사 회관")
            "q5_herbal" -> l(lang, "Phố Thuốc Bắc Triệu Quang Phục & Hào Sĩ Phường", "Cho Lon Herbal Medicine Quarter & Hao Si Phuong", "堤岸百草中药名巷与豪士坊百年古居", "チョロン漢方薬通り＆ハオシーフオン百年集落", "쩌롱 한약재 골목 & 하오시프엉 백년 고택")
            "q3_bunker" -> l(lang, "Biệt Động Sài Gòn & Hầm Bí Mật", "Secret Commandos & Underground Bunker", "西贡特工地下军火库寻踪", "サイゴン別動隊＆秘密地道ヘム", "사이공 특공대 비밀 기지 골목")
            "q4_riverfront" -> l(lang, "Hẻm Bến Vân Đồn, Cầu Mống & Bến Xưa Sài Gòn", "Ben Van Don Alleys, Cau Mong & Historic Riverfront", "云屯码头深巷、彩虹古桥与西贡水岸风情", "ベンヴァンドン運河路地＆コーモン歴史鉄橋", "벤반돈 강변 골목 & 꺼우몽 역사 철교")
            else -> l(lang, "Cà Phê Vợt & Hẻm Di Sản Sài Gòn", "Net Filter Coffee & Saigon Heritage Alleys", "西贡网滤咖啡与经典历史胡同", "隠れ路地＆伝統のネット珈琲", "사이공 숨은 골목 & 헤리티지 커피")
        }

        val enrichedStops = stops.map { stop ->
            val streetViewPhotos = com.example.util.StopPhotosHelper.getPhotos(stop)
            stop.copy(
                photos = streetViewPhotos,
                photoUri = streetViewPhotos.firstOrNull() ?: stop.photoUri
            )
        }

        return Quest(
            id = com.example.util.IdGenerator.generateQuestId(
                locationName = request.startingLocationName,
                title = title,
                category = questType
            ),
            title = title,
            theme = l(lang, "Văn hóa địa phương", "Local culture", "在地文化", "地域文化", "현지 문화"),
            summary = l(lang, "Khám phá những nét độc đáo và văn hóa hẻm phong phú.", "Explore unique alleyway heritage and local culture.", "探索胡志明市经典巷弄独特的风土人情与隐秘故事。", "ホーチミン市の魅力的な隠れた路地裏遺産を訪ねる。", "호치민시의 독특한 골목 문화와 역사적 유산을 탐방합니다."),
            estimatedMinutes = request.durationMinutes.coerceIn(30, 120),
            estimatedDistanceMetres = 2450,
            greenScore = GreenScore(
                score = 95,
                factors = listOf(
                    GreenFactor(
                        label = l(lang, "Đi Bộ", "Walkable", "适合步行", "徒歩散策", "도보 탐방"),
                        explanation = l(lang, "Đường bộ thân thiện môi trường.", "Pedestrian friendly and eco-friendly.", "路线紧凑，非常适合低碳步行。", "歩行者に優しいエコなルート。", "걷기에 편리하고 친환경적인 코스.")
                    )
                )
            ),
            stops = enrichedStops
        )
    }
}
