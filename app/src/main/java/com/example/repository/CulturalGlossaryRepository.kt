package com.example.repository

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.model.CulturalGlossaryItem
import com.example.model.GlossaryCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object CulturalGlossaryRepository {

    private const val TAG = "CulturalGlossaryRepo"
    private const val COLLECTION_NAME = "cultural_glossary"

    private val firestore: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }

    val triviaSpotlights: List<Pair<String, String>> = listOf(
        "💡 Bạn có biết?" to "Hơn 85% cư dân Sài Gòn sinh sống trong các con hẻm, tạo nên mạng lưới vi cộng đồng độc nhất vô nhị ở Đông Nam Á.",
        "🛵 Bí mật 'Xẹt'?" to "Số nhà có nhiều 'xẹt' nhất từng được ghi nhận ở Sài Gòn có đến 7 dấu gạch chéo (/), dẫn sâu vào lòng mê cung hẻm.",
        "☕ Cà phê vợt 70 năm" to "Siêu đất nung đun trên than hồng giữ cho hương vị cà phê vợt luôn ấm nồng và dậy mùi thơm béo tự nhiên.",
        "🥛 Nguồn gốc Bạc Xỉu" to "Từ cụm từ 'Bạc tẩy xỉu phé' (ly sữa nóng thêm chút cà phê) của cộng đồng người Hoa Chợ Lớn thập niên 1950.",
        "🏮 Làng lồng đèn Hòa Bình" to "Nghề làm lồng đèn giấy kiếng truyền thống tại Phường Hòa Bình có nguồn gốc từ làng Báo Đáp (Nam Định) từ thập niên 1950.",
        "🏢 Chung cư 14 Tôn Thất Đạm" to "Tòa nhà xây dựng từ thời Pháp thuộc năm 1886 nay là tụ điểm sáng tạo với các quán cà phê indie và studio nghệ thuật.",
        "🎸 Phố đàn Nguyễn Thiện Thuật" to "Quy tụ hàng chục xưởng đẽo gọt gỗ và chế tác đàn guitar, mandolin thủ công gia truyền qua 3 thế hệ.",
        "🟩 Gạch bông di sản" to "Những viên gạch xi măng ép hoa văn thủ công thế kỷ 20 vẫn còn vẹn nguyên trong các hành lang cư xá cổ."
    )

    private val DEFAULT_ITEMS: List<CulturalGlossaryItem> = listOf(
        CulturalGlossaryItem(
            id = "hem",
            term = "Hẻm",
            phonetic = "[hɛm³³] / [hɛm⁵⁵]",
            toneGuide = "Thanh ngã (Hỏi/Ngã) theo ngữ âm Nam Bộ phát âm nhẹ và ấm",
            category = GlossaryCategory.GEOGRAPHY,
            icon = "🛵",
            accentColor = Color(0xFF00B14F),
            shortDefinitionVi = "Mạng lưới ngõ ngách đan xen phía sau các đại lộ chính Sài Gòn, nơi sinh sống của hơn 85% cư dân.",
            shortDefinitionEn = "The dense network of residential alleyways behind Saigon's main avenues, home to 85%+ of locals.",
            shortDefinitionZh = "胡志明市大道背后交错密布的深巷网络，居住着85%以上的当地居民。",
            shortDefinitionJa = "サイゴンの大通り裏に網の目のように広がる居住路地網。住民の85%以上が暮らす。",
            shortDefinitionKo = "사이공 대로 뒤편에 밀집된 주거 골목길 네트워크. 주민의 85% 이상이 거주함.",
            fullDescriptionVi = "Hẻm không chỉ là lối đi giao thông hẹp mà là không gian sinh hoạt cộng đồng thu nhỏ của người Sài Gòn. Trong hẻm có quán cà phê cóc, gánh hàng rong, tiệm tạp hóa và những nụ cười chào hỏi quen thuộc.",
            fullDescriptionEn = "In Saigon, a 'Hẻm' is far more than a narrow passage; it is a micro-neighborhood and a shared living room. Alleys host sidewalk coffee stalls, noodle carts, corner grocery shops, and vibrant daily social interactions away from street traffic.",
            fullDescriptionZh = "在胡志明市，“Hẻm”（胡同）绝不仅是狭窄的通道，更是西贡人的共享生活空间。胡同里藏着露天咖啡摊、流动小吃摊、杂货店以及避开主干道喧嚣的街坊温情。",
            fullDescriptionJa = "サイゴンにおいて「Hẻm（ヘム）」は単なる狭い通路ではなく、人々の生活の場です。路地には路上カフェ、屋台、商店が立ち並び、大通りの喧騒から離れた温かいコミュニティが形成されています。",
            fullDescriptionKo = "사이공에서 'Hẻm(골목)'은 단순한 보행로가 아닌 공동 생활 공간입니다. 골목 안에는 노천 카페, 길거리 음식 수레, 구멍가게가 자리하고 있어 도심의 소음에서 벗어난 정겨운 일상을 보여줍니다.",
            whyItMattersVi = "Tại sao địa điểm có tên Hẻm? Vì những kho báu di sản, ẩm thực ngon nhất Sài Gòn đều giấu mình trong lòng hẻm thay vì mặt phố lớn.",
            whyItMattersEn = "Why are destinations labeled 'Hẻm'? Because Saigon's richest heritage, best street food, and authentic local life hide deep inside alleys rather than commercial storefronts.",
            whyItMattersZh = "为什么许多打卡点都标有“Hẻm”？因为西贡最地道的美食、最具历史韵味的古迹和人文温情，都隐匿于深巷之中而非繁华路边店。",
            whyItMattersJa = "なぜ多くのスポットに「Hẻm」とつくのか？サイゴンの最も美味しいストリートフードや歴史的遺産は、大通りではなく路地の奥深くに隠されているからです。",
            whyItMattersKo = "왜 장소 이름에 'Hẻm'이 붙어 있을까요? 사이공 최고의 먹거리와 역사 유적, 진짜 로컬 삶은 대로변이 아닌 골목 깊숙한 곳에 숨어있기 때문입니다.",
            triviaVi = "Mỗi con hẻm tại Sài Gòn đều có 'trưởng hẻm' hoặc các cô bác lớn tuổi đóng vai trò như người bảo vệ văn hóa cộng đồng.",
            triviaEn = "Most alleys have respected elder stewards who keep the alley safe, friendly, and clean for generations.",
            exampleLocationsVi = listOf("Hẻm 158 Pasteur (P. Sài Gòn)", "Hẻm 287 Nguyễn Đình Chiểu (P. Bàn Cờ)", "Hẻm 206 Trần Hưng Đạo (P. Chợ Quán)"),
            exampleLocationsEn = listOf("Alley 158 Pasteur (Saigon Ward)", "Alley 287 Nguyen Dinh Chieu (Ban Co Ward)", "Alley 206 Tran Hung Dao (Cho Quan Ward)")
        ),
        CulturalGlossaryItem(
            id = "xet",
            term = "Xẹt (Dấu gạch chéo /)",
            phonetic = "[sɛt³⁵] / Slash /",
            toneGuide = "Thanh sắc - âm ngắt dứt khoát",
            category = GlossaryCategory.GEOGRAPHY,
            icon = "📍",
            accentColor = Color(0xFF0288D1),
            shortDefinitionVi = "Thuật ngữ miền Nam chỉ dấu gạch chéo (/) trong số nhà, thể hiện độ sâu của địa chỉ trong lòng hẻm.",
            shortDefinitionEn = "Southern slang for slash marks (/) in house numbers, indicating address depth inside alley turns.",
            shortDefinitionZh = "越南南部对门牌号中斜杠（/）的俗称，用以表示地址进入巷弄的分歧深度。",
            shortDefinitionJa = "番地の中の「/（スラッシュ）」を表す南部の方言。路地を何回曲がった奥にあるかを示す。",
            shortDefinitionKo = "번지수의 슬래시(/)를 나타내는 남부 방언. 골목 안으로 얼마나 깊이 들어가는지를 표시함.",
            fullDescriptionVi = "Mỗi 'xẹt' đại diện cho một lần rẽ vào nhánh hẻm nhỏ hơn. Ví dụ: Địa chỉ '287/10/2 Nguyễn Đình Chiểu' nghĩa là từ đường lớn 287 rẽ vào hẻm 1 (xẹt 1), sau đó rẽ tiếp vào hẻm phụ thứ hai (xẹt 2). Sài Gòn có những ngôi nhà tới 5-6 xẹt!",
            fullDescriptionEn = "Each 'xẹt' (slash) represents a turn into a deeper sub-alley branch. For example, '287/10/2' means: Main Street #287 -> Turn into alley 10 (1st xẹt) -> Turn into sub-alley 2 (2nd xẹt). Some maze-like alleys in Saigon feature up to 5 or 6 xẹts!",
            fullDescriptionZh = "每一个“xẹt”（斜杠）代表转入一次更细分的巷弄。例如门牌“287/10/2”意为：从主街287号进入第10号巷（第1斜杠），再转入第2号小巷（第2斜杠）。西贡有些复杂胡同甚至多达5-6个斜杠！",
            fullDescriptionJa = "スラッシュ「xẹt」1つごとに、さらに細い枝路地へと折れ曲がることを意味します。例えば「287/10/2」は、大通り287号から10号路地に入り（1xẹt）、さらに2号枝路地に入る（2xẹt）ことを示します。",
            fullDescriptionKo = "각 슬래시('xẹt')는 더 깊은 지골목으로 꺾어 들어감을 의미합니다. 예: '287/10/2'는 대로 287번지에서 10번 골목으로 진입한 후(첫 번째 xẹt), 다시 2번 세골목으로 들어가는(두 번째 xẹt) 방식입니다.",
            whyItMattersVi = "Giải thích ký hiệu địa chỉ: Giúp người tham quan dễ dàng đọc hiểu bản đồ số nhà và không bị lạc trong mê cung hẻm.",
            whyItMattersEn = "Decodes Saigon's unique address format so travelers can easily read alley maps and navigate deep number sequences.",
            whyItMattersZh = "破解西贡独特的门牌逻辑：帮助游客轻松读懂地图地址，不再迷失于繁复的巷弄迷宫中。",
            whyItMattersJa = "サイゴンのユニークな住所表示を解読。マップを見ながら路地の奥深くでも迷わずに目的地に到達できます。",
            whyItMattersKo = "사이공 특유의 주소 체계를 이해하면 골목 복잡한 지도에서도 길을 잃지 않고 목적지를 찾을 수 있습니다.",
            triviaVi = "Khi số nhà có chữ cái (vd: 18A, 18B), đó là những nhà được chia nhỏ từ khu đất gốc trước đây.",
            triviaEn = "Letter suffixes like '18A' indicate historic subdividing of French colonial land plots.",
            exampleLocationsVi = listOf("287/10 Nguyễn Đình Chiểu (P. Bàn Cờ)", "18A/15 Nguyễn Thị Minh Khai (P. Tân Định)"),
            exampleLocationsEn = listOf("287/10 Nguyen Dinh Chieu (Ban Co Ward)", "18A/15 Nguyen Thi Minh Khai (Tan Dinh Ward)")
        ),
        CulturalGlossaryItem(
            id = "hem_cut",
            term = "Hẻm Cụt & Hẻm Thông",
            phonetic = "[hɛm³³ kut³⁵] / Cul-de-sac",
            toneGuide = "Thanh nặng - âm trầm, dứt",
            category = GlossaryCategory.GEOGRAPHY,
            icon = "🚪",
            accentColor = Color(0xFF10B981),
            shortDefinitionVi = "Hẻm cụt là ốc đảo yên tĩnh nơi trẻ nhỏ vui chơi; hẻm thông là mê cung đường tắt né kẹt xe của người Sài Gòn.",
            shortDefinitionEn = "Cul-de-sacs are peaceful communal safe zones; through-alleys are secret motorbike bypass shortcuts.",
            shortDefinitionZh = "死胡同是安宁的邻里绿洲与儿童乐园；贯通巷则是骑行者避开主干道拥堵的秘密捷径。",
            shortDefinitionJa = "行き止まり路地は子どもが遊ぶ静かな憩いの場。通り抜け路地は渋滞を避ける秘密の近道。",
            shortDefinitionKo = "막다른 골목은 아이들이 뛰노는 조용한 쉼터이며, 관통 골목은 교통체증을 피하는 현지인들의 지름길입니다.",
            fullDescriptionVi = "Người Sài Gòn có câu: 'Hẻm cụt nhưng tình người không cụt'. Những con hẻm cụt thường có mức độ gắn kết hàng xóm cao nhất, cửa nhà mở toang không sợ trộm. Ngược lại, hẻm thông (xuyên qua nhiều phường) là bản đồ nằm lòng của các bác tài xe ôm.",
            fullDescriptionEn = "Saigon locals often say: 'Dead-end alley, but never a dead-end for warmth'. Cul-de-sacs cultivate the closest neighborly bonds where doors stay open all day. In contrast, interconnecting through-alleys are intricate secret bypasses mastered by local scooter riders.",
            fullDescriptionZh = "西贡有一句老话：“胡同虽到头，人情永不尽”。死胡同内的街坊邻里关系极为亲密，白天家家户户敞开大门。而四通八达的贯通巷则是当地老摩托车司机的秘密路网。",
            fullDescriptionJa = "「行き止まりでも温もりは行き止まらない」と言われるほど、袋小路の路地は住民同士の絆が強く安心です。一方、貫通路地はサイゴンのライダーたちが大通りの混雑を迂回する抜け道です。",
            fullDescriptionKo = "사이공 사람들은 '막다른 골목일지라도 정은 막히지 않는다'고 말합니다. 막다른 골목은 이웃 간의 유대가 가장 끈끈하며, 관통 골목은 오토바이 운전자들의 숨은 우회로 역할을 합니다.",
            whyItMattersVi = "Giúp bạn tự tin khám phá: Biết cách phân biệt hẻm an toàn để tản bộ ngắm cảnh và hẻm xuyên để di chuyển nhanh.",
            whyItMattersEn = "Guides your stroll: Distinguish between tranquil dead-ends ideal for peaceful photos and bustling shortcut arteries.",
            whyItMattersZh = "指引漫步路线：助你区分适合悠闲摄影的宁静死胡同与适合快速通行的穿街小巷。",
            whyItMattersJa = "散策のヒント：のんびり写真を撮れる静かな路地と、素早く移動できる抜け道を使い分けられます。",
            whyItMattersKo = "산책 가이드: 조용히 사진 찍기 좋은 한적한 막다른 골목과 빠른 이동을 돕는 관통 골목을 구분할 수 있습니다.",
            triviaVi = "Nhiều hẻm thông ở Phường Bàn Cờ có kết cấu ô bàn cờ thông minh được thiết kế từ thời Pháp thuộc.",
            triviaEn = "The chessboard-style grid alleys in Ban Co Ward were intentionally mapped to maximize airflow and connectivity.",
            exampleLocationsVi = listOf("Hẻm Bàn Cờ (P. Bàn Cờ)", "Hẻm 142 Trần Quốc Thảo (P. Xuân Hòa)"),
            exampleLocationsEn = listOf("Ban Co Chessboard Alleys (Ban Co Ward)", "Alley 142 Tran Quoc Thao (Xuan Hoa Ward)")
        ),
        CulturalGlossaryItem(
            id = "ca_phe_vot",
            term = "Cà Phê Vợt",
            phonetic = "[kaː³¹ fe³³ vɔt³⁵]",
            toneGuide = "Thanh huyền - không dấu - thanh nặng",
            category = GlossaryCategory.CULINARY,
            icon = "☕",
            accentColor = Color(0xFFD97706),
            shortDefinitionVi = "Phương pháp pha cà phê bằng vợt vải truyền thống lưu giữ từ thập niên 1950 trong lòng hẻm Sài Gòn.",
            shortDefinitionEn = "Traditional cloth net-filter coffee brewing technique preserved since the 1950s in Saigon's alleys.",
            shortDefinitionZh = "自20世纪50年代起留存至今的西贡传统布袋网滤冲泡咖啡，多藏于古老巷弄中。",
            shortDefinitionJa = "1950年代からサイゴンの路地裏で受け継がれてきた、布製フィルター（網）で淹れる伝統の珈琲。",
            shortDefinitionKo = "1950년대부터 사이공 골목에서 이어져 온 전통 헝겊 그물 필터 추출 커피.",
            fullDescriptionVi = "Cà phê bột được ủ trong túi vợt vải dài, nhúng liên tục vào siêu đất nung đun trên bếp than hồng. Cách pha này tạo ra vị cà phê sần sật, thơm đậm đà dịu nhẹ và lớp bọt sánh tự nhiên mà phin kim loại không có được.",
            fullDescriptionEn = "Coarse coffee grounds are steeped in long cloth mesh socks inside terracotta clay pots resting over live charcoal stoves. This 70-year-old method extracts a smooth, rich aroma with natural silky body distinct from metal phin filters.",
            fullDescriptionZh = "将咖啡粉装入长形布袋中，放入浸泡在炭火陶罐中的沸水中反复萃取。这种传承70年的古法冲泡，能萃取出一股温润顺滑、回甘悠长的独特的醇香，非金属滤网所能比拟。",
            fullDescriptionJa = "炭火で温められた素焼きの土罐の中で、長めの布フィルターに豆を入れて抽出します。金属ドリッパーにはない、まろやかで芳醇な香りとコクが引き出されます。",
            fullDescriptionKo = "숯불 위 토기에 긴 헝겊 그물망을 넣고 커피를 우려내는 방식입니다. 금속 핀(Phin) 필터와 달리 은은하고 부드러운 향과 깊은 풍미를 자랑합니다.",
            whyItMattersVi = "Văn hóa thưởng thức: Giúp bạn hiểu vì sao người Sài Gòn mê mẩn ngồi ghế súp nhựa trong hẻm thưởng thức ly cà phê vợt nóng hổi.",
            whyItMattersEn = "Cultural insight: Explains why locals love sitting on tiny plastic stools inside quiet alleys sipping hot net-brewed coffee at sunrise.",
            whyItMattersZh = "品味生活方式：理解为什么当地人钟爱坐在胡同的小塑料凳上，悠闲地品尝一杯现冲的热网滤咖啡。",
            whyItMattersJa = "カフェ文化の体験：地元の人が朝早くから路地の小さなプラスチック椅子に座り、網フィルター珈琲を楽しむ理由が分かります。",
            whyItMattersKo = "문화적 이해: 현지인들이 왜 아침 일찍 골목 안 플라스틱 의자에 앉아 따뜻한 그물 커피를 즐기는지 알 수 있습니다.",
            triviaVi = "Quán cà phê vợt Ba Lù tại Chợ Lớn vẫn rang cà phê bằng bơ và mỡ gà thủ công theo công thức gia truyền hơn 70 năm.",
            triviaEn = "Ba Lu Net Coffee in Cho Lon still roasts beans with traditional butter in an antique hand-crank drum.",
            exampleLocationsVi = listOf("Quán Cà Phê Vợt Hẻm 330 Phan Đình Phùng (P. Đức Nhuận)", "Cà Phê Vợt Ba Lù Hẻm 118 Bùi Hữu Nghĩa (P. Chợ Lớn)"),
            exampleLocationsEn = listOf("Net Coffee Alley 330 Phan Dinh Phung (Duc Nhuan Ward)", "Ba Lu Net Coffee Alley 118 Bui Huu Nghia (Cho Lon Ward)")
        ),
        CulturalGlossaryItem(
            id = "ca_phe_coc",
            term = "Cà Phê Cóc & Bàn Ghế Nhựa",
            phonetic = "[kaː³¹ fe³³ kɔk³⁵]",
            toneGuide = "Thanh sắc dứt khoát - mô phỏng dáng ngồi cóc chồm hổm",
            category = GlossaryCategory.CULINARY,
            icon = "🪑",
            accentColor = Color(0xFFE11D48),
            shortDefinitionVi = "Văn hóa cà phê vỉa hè bình dân với những chiếc ghế nhựa lùn - biểu tượng bình đẳng và cởi mở của đất Sài Gòn.",
            shortDefinitionEn = "Sidewalk low-stool coffee stalls: Saigon's symbol of casual egalitarianism and street social life.",
            shortDefinitionZh = "街头塑料小矮凳咖啡文化，西贡平等包容与市井闲适生活的生动缩影。",
            shortDefinitionJa = "低いプラスチック椅子に腰掛ける路上カフェ。サイゴンの平等で自由なストリート文化の象徴。",
            shortDefinitionKo = "낮은 플라스틱 의자에 앉아 마시는 노천 카페 문화로, 사이공의 평등하고 열린 소통의 상징입니다.",
            fullDescriptionVi = "Từ 'cóc' bắt nguồn từ tư thế ngồi xổm (ngồi cóc) trên những chiếc ghế đẩu nhựa thấp sát mặt đất. Tại quán cóc trong hẻm, giám đốc, nghệ sĩ, sinh viên và người lao động đều ngồi chung một không gian, trò chuyện rôm rả bên ly cà phê đá 15k.",
            fullDescriptionEn = "Named after the crouching 'toad-like' posture on low plastic stools. Alley coffee spots are the great levelers of Saigon: executives, artists, students, and street vendors sit elbow-to-elbow, discussing news over a $0.60 glass of iced coffee.",
            fullDescriptionZh = "“Cóc”源自蹲在低矮塑料凳上如蟾蜍般的坐姿。在巷弄咖啡摊前，企业高管、艺术家、青年学生与街头劳动者并肩而坐，就着一杯冰咖啡畅谈天下事。",
            fullDescriptionJa = "「コック（カエル）」のように低い椅子に小さく腰掛ける姿から名付けられました。社長も学生も労働者も肩を並べて世間話に花を咲かせる、最もサイゴンらしい風景です。",
            fullDescriptionKo = "낮은 플라스틱 의자에 웅크리고 앉은 모습이 두꺼비(Cóc) 같다고 하여 붙여진 이름입니다. 지위 고하를 막론하고 모두가 나란히 앉아 얼음 커피를 마시며 담소를 나눕니다.",
            whyItMattersVi = "Chìa khóa hòa nhập: Bí quyết để hòa mình vào nhịp sống chân thực nhất của người dân địa phương mà không có bất kỳ rào cản nào.",
            whyItMattersEn = "Social key: The fastest way to immerse into authentic neighborhood life without pretension or social barriers.",
            whyItMattersZh = "融入地道生活：无需任何拘束，即可瞬间融入最鲜活真实的西贡本地社区日常。",
            whyItMattersJa = "ローカル体験の極意：気取らずにサイゴンのありのままの暮らしに溶け込む一番の近道です。",
            whyItMattersKo = "로컬 체험의 핵심: 아무런 격식 없이 현지인들의 살아있는 일상 속으로 가장 자연스럽게 스며드는 방법입니다.",
            triviaVi = "Mỗi ly cà phê cóc luôn được tặng kèm một ly 'trà đá' mát lạnh miễn phí châm liên tục.",
            triviaEn = "Every cup of alley coffee is traditionally served with complimentary, endlessly refilled iced jasmine tea ('Trà Đá').",
            exampleLocationsVi = listOf("Hẻm 158 Pasteur (P. Sài Gòn)", "Hẻm 18A Nguyễn Thị Minh Khai (P. Tân Định)"),
            exampleLocationsEn = listOf("Alley 158 Pasteur (Saigon Ward)", "Alley 18A Nguyen Thi Minh Khai (Tan Dinh Ward)")
        ),
        CulturalGlossaryItem(
            id = "biet_dong",
            term = "Biệt Động Sài Gòn",
            phonetic = "[biət³⁵ ɗowŋ͡m³⁵ saːj³¹ ɣɔːn³¹]",
            toneGuide = "Thanh nặng - thanh nặng - thanh huyền - thanh huyền",
            category = GlossaryCategory.HERITAGE,
            icon = "🏛️",
            accentColor = Color(0xFF8B5CF6),
            shortDefinitionVi = "Lực lượng tình báo & chiến sĩ đặc công đô thị hoạt động bí mật dưới các căn hầm ngầm trong hẻm phố.",
            shortDefinitionEn = "Underground urban resistance rangers operating secret weapon cellars beneath innocent alley houses.",
            shortDefinitionZh = "隐藏于西贡普通胡同民居地下军火库中秘密行动的城市特工与情报人员。",
            shortDefinitionJa = "普通の路地住宅の地下に隠された秘密の武器庫から活動した都市地下抵抗部隊（サイゴン別動隊）。",
            shortDefinitionKo = "일반 골목 주택 지하의 비밀 기지에서 활동했던 도시 지하 특공대.",
            fullDescriptionVi = "Trong thời kỳ kháng chiến, các chiến sĩ Biệt Động đã biến những ngôi nhà nằm sâu trong hẻm thành căn hầm giấu vũ khí, xưởng in tài liệu và đường hầm thoát hiểm ngay dưới lòng thành phố mà đối phương không thể phát hiện.",
            fullDescriptionEn = "During the war years, urban rangers transformed ordinary alley houses into clandestine armories, underground printing presses, and secret escape hatches carved beneath living room floorboards right under the radar of occupiers.",
            fullDescriptionZh = "在战争时期，别动队特工将深巷中的普通民宅改造成地下武器库、秘密印刷所和藏匿在客厅地板下的逃生通道，在市中心构建起难以察觉的地下网络。",
            fullDescriptionJa = "戦時中、サイゴン別動隊は路地の奥にあるごく普通の民家を地下武器庫や秘密印刷所へと改造し、リビングの床下に隠し穴を掘って活動していました。",
            fullDescriptionKo = "전쟁 당시 사이공 특공대는 골목 깊숙한 일반 주택 거실 바닥 아래에 비밀 무기고와 탈출용 지하 터널을 구축하여 비밀 작전을 수행했습니다.",
            whyItMattersVi = "Di sản lịch sử: Nhận diện những địa danh di tích bí mật ẩn sau các quán cà phê hay nhà dân trong hẻm.",
            whyItMattersEn = "Historical discovery: Helps users recognize heroic resistance sites hidden behind unassuming coffee shops and residential alley homes.",
            whyItMattersZh = "历史探索指南：帮助游客辨识隐藏于巷弄咖啡馆与普通住宅背后的传奇历史遗迹。",
            whyItMattersJa = "歴史散策の鍵：路地裏の素朴なカフェや民家に隠された、歴史的な秘密遺迹を発見できます。",
            whyItMattersKo = "역사 탐방의 핵심: 평범한 골목 카페나 주택 뒤에 숨겨진 비밀 역사 유적지를 알아보는 데 도움을 줍니다.",
            triviaVi = "Căn hầm tại hẻm 287 Nguyễn Đình Chiểu chứa được gần 2 tấn vũ khí ngay giữa trung tâm thành phố.",
            triviaEn = "The secret bunker in Ban Co Ward concealed nearly 2 tons of ammunition right beneath family dining tables.",
            exampleLocationsVi = listOf("Căn Hầm Bí Mật Hẻm 287 Nguyễn Đình Chiểu (P. Bàn Cờ)", "Cà Phê Đỗ Phủ Hẻm Đặng Dung (P. Tân Định)"),
            exampleLocationsEn = listOf("Secret Bunker Alley 287 Nguyen Dinh Chieu (Ban Co Ward)", "Do Phu Ranger Cafe Dang Dung Alley (Tan Dinh Ward)")
        ),
        CulturalGlossaryItem(
            id = "cho_lon",
            term = "Chợ Lớn (Đô thị Di sản)",
            phonetic = "[cɔː³¹ ləːn³⁵] / Chinatown",
            toneGuide = "Thanh huyền - thanh sắc",
            category = GlossaryCategory.COMMUNITY,
            icon = "🏮",
            accentColor = Color(0xFFFF5722),
            shortDefinitionVi = "Khu đô thị - thương cảng di sản người Hoa lâu đời nổi tiếng với hẻm hội quán & ẩm thực đậm chất Nam Bộ.",
            shortDefinitionEn = "Saigon's historic Chinatown famous for Cantonese guild halls, herbal alleys, and rich food heritage.",
            shortDefinitionZh = "百年华人历史街区，以会馆古庙、中药巷弄与特色美食闻名。",
            shortDefinitionJa = "サイゴンの中華街。会館や漢方薬路地、中華グルメで有名。",
            shortDefinitionKo = "사이공의 역사적인 차이나타운. 회관과 한약 골목, 먹거리로 유명함.",
            fullDescriptionVi = "Được hình thành từ thế kỷ 18 bởi cộng đồng người Hoa Quảng Đông, Triều Châu, Phúc Kiến. Chợ Lớn sở hữu kiến trúc mái ngói âm dương, các dãy nhà phố thương mại kết hợp hẻm sinh hoạt chung đong đầy bản sắc.",
            fullDescriptionEn = "Established in the late 18th century by Cantonese, Teochew, and Hokkien settlers, Chợ Lớn features tiled yin-yang roofs, historic shophouse alleys, traditional herbal medicine dispensaries, and vibrant temple yards.",
            fullDescriptionZh = "于18世纪由广府、潮州、福建等华人移民建立。堤岸拥有阴阳瓦顶、骑楼老街、中药老铺以及热闹非凡的寺庙会馆，散发着浓郁的传统文化气息。",
            fullDescriptionJa = "18世紀後半に広東、潮州、福建からの移民によって作られた街。陰陽瓦の屋根や伝統的な町屋路地、漢方薬局、華やかな天后宮などが立ち並びます。",
            fullDescriptionKo = "18세기 광둥, 초주, 복건 출신 화교들이 형성한 지역입니다. 음양 기와지붕과 저택 골목, 한약방, 화려한 사원이 어우러져 독특한 분위기를 자아냅니다.",
            whyItMattersVi = "Giải thích tên gọi khu vực: Giúp người xem hiểu sự khác biệt kiến trúc & văn hóa giữa khu vực Chợ Lớn và trung tâm Sài Gòn.",
            whyItMattersEn = "Spatial contextualization: Explains the architectural and culinary shift when moving from central Saigon to Cantonese heritage Chợ Lớn.",
            whyItMattersZh = "区域文化理解：阐明从西贡市中心过渡到浓厚粤华色彩的堤岸街区时的建筑与文化差异。",
            whyItMattersJa = "エリアの理解：中心部サイゴンから、中華文化の濃いチョロン（Chợ Lớn）への建築や文化の変化を理解できます。",
            whyItMattersKo = "지역 이해: 중심가 사이공에서 화교 문화의 쩌롱으로 이동할 때의 건축 및 문화적 차이를 이해할 수 있습니다.",
            triviaVi = "Tên gọi 'Sài Gòn' ban đầu từng được dùng để chỉ riêng khu vực Chợ Lớn trước khi mở rộng ra toàn thành phố.",
            triviaEn = "The name 'Saigon' originally referred specifically to the bustling commercial settlement of Cho Lon.",
            exampleLocationsVi = listOf("Hẻm 206 Trần Hưng Đạo (P. Chợ Quán)", "Hẻm 191 Hà Tôn Quyền (P. Chợ Lớn)", "Hội Quán Nghĩa An (P. Chợ Lớn)"),
            exampleLocationsEn = listOf("Alley 206 Tran Hung Dao (Cho Quan Ward)", "Alley 191 Ha Ton Quyen (Cho Lon Ward)", "Nghia An Assembly Hall (Cho Lon Ward)")
        ),
        CulturalGlossaryItem(
            id = "hao_si_phuong",
            term = "Hào Sĩ Phường",
            phonetic = "[haːw³¹ si³⁵ fɨəŋ³¹]",
            toneGuide = "Thanh huyền - thanh sắc - thanh huyền",
            category = GlossaryCategory.ARCHITECTURE,
            icon = "🏘️",
            accentColor = Color(0xFF0D9488),
            shortDefinitionVi = "Con hẻm chung cư cổ trăm tuổi của cộng đồng người Hoa với kiến trúc ban công gỗ hai tầng sơn xanh ngọc.",
            shortDefinitionEn = "Centenarian Cantonese courtyard tenement alley featuring twin wooden walkways and pastel turquoise shutters.",
            shortDefinitionZh = "拥有百年历史的岭南骑楼庭院式老巷，以独特的双层木质连廊与薄荷绿百叶窗闻名。",
            shortDefinitionJa = "築100年の中華系集合住宅路地。ターコイズブルーの木製窓と2階回廊が特徴的。",
            shortDefinitionKo = "100년 역사의 화교 연립주택 골목으로, 민트색 목조 덧문과 2층 복도 구조가 인상적입니다.",
            fullDescriptionVi = "Xây dựng từ năm 1910 bởi một thương nhân họ Hứa. Hào Sĩ Phường có cấu trúc khép kín với hai dãy nhà đối diện nhau, cầu thang chung ở giữa và các bàn thờ Thiên Quan trước mỗi hiên nhà. Đây là bối cảnh kinh điển của nhiều bộ phim điện ảnh.",
            fullDescriptionEn = "Constructed around 1910, Hao Si Phuong is a self-contained courtyard enclave with two facing blocks connected by a shared suspended wooden balcony. Shrines to the Sky God sit beside brightly painted pastel doors in timeless cinematic harmony.",
            fullDescriptionZh = "建于1910年左右，由两排相对而建的双层楼房组成，中间由公共木质走廊连接。每户门前供奉着天官神位，浓郁的旧港风与岭南风情使其成为无数电影的经典取景地。",
            fullDescriptionJa = "1910年頃に建設された袋小路状の集合住宅。向かい合う2棟が木製の空中回廊で結ばれ、各戸の玄関には小さな祭壇が祀られています。映画のロケ地としても名高い場所です。",
            fullDescriptionKo = "1910년경 지어진 2층 목조 테라스 구조의 안마당형 주택 단지입니다. 마주 보는 주택 사이에 연결 복도가 있고 문마다 작은 제단이 놓여 있어 클래식한 영화 속 분위기를 풍깁니다.",
            whyItMattersVi = "Bảo tồn di sản: Điểm tham quan kiến trúc cộng cư đặc trưng, cần tôn trọng sự tĩnh lặng và đời sống riêng tư của cư dân.",
            whyItMattersEn = "Heritage etiquette: A prime example of shared communal heritage; visitors should tread softly and respect residents' peace.",
            whyItMattersZh = "建筑遗迹保护：典型的百年社区建筑样本，漫步探访时请保持轻声细语，尊重居民宁静生活。",
            whyItMattersJa = "文化マナー：貴重な共同住宅遺産です。見学時は住民の静かな暮らしを尊重し、静かに撮影しましょう。",
            whyItMattersKo = "문화 에티켓: 소중한 백 년 공동체 유산이므로 주민들의 일상을 배려하여 조용히 관람해야 합니다.",
            triviaVi = "Tên 'Hào Sĩ Phường' có nghĩa là nơi hội tụ của những con người hào sảng, trọng nghĩa tình và gắn bó keo sơn.",
            triviaEn = "The name translates poetically to 'The Ward of Generous and Chivalrous Neighbors'.",
            exampleLocationsVi = listOf("Hẻm 206 Trần Hưng Đạo (P. Chợ Quán)"),
            exampleLocationsEn = listOf("Alley 206 Tran Hung Dao (Cho Quan Ward)")
        ),
        CulturalGlossaryItem(
            id = "chung_cu_co",
            term = "Chung Cư Cũ & Nghệ Thuật Độc Lập",
            phonetic = "[cuŋ͡m³³ kɨ³³ kɔː³¹³]",
            toneGuide = "Thanh ngang - thanh ngang - thanh ngã",
            category = GlossaryCategory.ARCHITECTURE,
            icon = "🏢",
            accentColor = Color(0xFF6366F1),
            shortDefinitionVi = "Những khu cư xá, chung cư thời Pháp và thập niên 1960 được giới trẻ 'tái sinh' thành không gian nghệ thuật & cà phê.",
            shortDefinitionEn = "French-era and 1960s apartment blocks reborn into vibrant hubs of indie cafes, galleries, and fashion boutiques.",
            shortDefinitionZh = "殖民时期与上世纪60年代的老旧公寓，如今被年轻一代改造成充满生机的独立咖啡馆与艺术空间。",
            shortDefinitionJa = "フランス統治期や60年代の古アパートを若者たちがリノベした、カフェやギャラリーが集まるお洒落スポット。",
            shortDefinitionKo = "식민지 시대와 1960년대 옛 아파트들이 청년들에 의해 개성 넘치는 인디 카페와 예술 공간으로 재탄생한 명소.",
            fullDescriptionVi = "Điển hình như Chung cư 14 Tôn Thất Đạm (xây từ 1886) hay 42 Nguyễn Huệ. Bước lên những bậc thang lát gạch bông cổ và dây điện chằng chịt, du khách sẽ bước vào thế giới của các quán cà phê phong cách retro, xưởng gốm và tiệm đĩa than.",
            fullDescriptionEn = "Iconic spots like 14 Ton That Dam (dating to 1886) feature winding cement staircases, antique encaustic cement floor tiles, and open corridors housing intimate specialty roasteries, indie ceramic workshops, and vintage vinyl listening dens.",
            fullDescriptionZh = "以建于1886年的孙室淡街14号古旧公寓为代表。踏上古老的彩色花砖楼梯，穿过斑驳的走廊，映入眼帘的是精品手冲咖啡馆、陶艺手作坊和复古黑胶唱片店。",
            fullDescriptionJa = "1886年築のトントゥックダム14番地アパートなどが代表例。レトロな幾何学セメントタイル階段を上がると、個性派ロースタリーカフェやレコードショップが広がります。",
            fullDescriptionKo = "1886년에 건축된 똔탓담 14번지 아파트가 대표적입니다. 낡은 시멘트 타일 계단을 오르면 개성 있는 핸드드립 카페, 도자기 공방, 빈티지 바이닐 샵이 맞이합니다.",
            whyItMattersVi = "Sự giao thoa thời gian: Trải nghiệm cách Sài Gòn gìn giữ ký ức kiến trúc xưa kết hợp hài hòa với hơi thở đương đại.",
            whyItMattersEn = "Urban regeneration: Discover how old architectural memory harmoniously coexists with contemporary creative youth culture.",
            whyItMattersZh = "新旧交融之美：亲身感受老西贡历史印记与现代年轻潮流文化碰撞出的生动火花。",
            whyItMattersJa = "時空の交差：古き良き建築美と現代の若者カルチャーが見事に共存する姿を体験できます。",
            whyItMattersKo = "시대의 조화: 옛 건축의 기억과 현대 청년들의 창의적 감성이 공존하는 독특한 매력을 경험할 수 있습니다.",
            triviaVi = "Gạch bông lát sàn tại các chung cư cổ này từng được nhập khẩu trực tiếp từ miền Nam nước Pháp từ thế kỷ 19.",
            triviaEn = "The decorative patterned encaustic tiles in these stairwells were originally shipped from Southern France in the 1890s.",
            exampleLocationsVi = listOf("Chung Cư 14 Tôn Thất Đạm (P. Sài Gòn)", "Chung Cư 42 Tôn Thất Đạm (P. Sài Gòn)", "Hẻm 142 Trần Quốc Thảo (P. Xuân Hòa)"),
            exampleLocationsEn = listOf("14 Ton That Dam Vintage Apartment (Saigon Ward)", "42 Ton That Dam Apartment (Saigon Ward)", "Alley 142 Tran Quoc Thao (Xuan Hoa Ward)")
        ),
        CulturalGlossaryItem(
            id = "hu_tieu_go",
            term = "Hủ Tiếu Gõ (Tiếng Cốc Cốc)",
            phonetic = "[hu³¹³ tiəw³⁵ ɣɔː³⁵]",
            toneGuide = "Thanh ngã - thanh sắc - thanh ngã",
            category = GlossaryCategory.STREET_LIFE,
            icon = "🍜",
            accentColor = Color(0xFFF97316),
            shortDefinitionVi = "Những xe mì hủ tiếu bình dân về đêm với âm thanh gõ thanh tre 'cốc cốc' vang vọng khắp các ngõ hẻm.",
            shortDefinitionEn = "Late-night alley noodle carts accompanied by rhythmic bamboo click-clack percussion ('cốc cốc').",
            shortDefinitionZh = "深夜巷弄里的平民面摊，伴随着穿透小巷的清脆竹板敲击声（“笃笃面”）。",
            shortDefinitionJa = "夜の路地裏に響く竹のカスタネット音「コッコッ」でお馴染みの、庶民派フーティウ屋台。",
            shortDefinitionKo = "대나무 막대를 '딱딱' 두드리는 소리와 함께 골목을 찾아오는 밤거리의 서민적인 쌀국수 수레.",
            fullDescriptionVi = "Các em nhỏ hoặc phụ quán cầm hai thanh tre gõ nhịp liên hồi đi sâu vào từng nhánh hẻm nhỏ. Ai muốn ăn chỉ cần gọi với ra cửa, vài phút sau một tô hủ tiếu nóng hổi với thịt nạc mỏng, tóp mỡ giòn rụm và hành phi thơm lừng sẽ được mang đến tận nơi.",
            fullDescriptionEn = "Young runners walk through winding alleys striking two bamboo sticks together in a resonant syncopated rhythm. Residents simply call out from their doorway, and within minutes, a steaming bowl of pork noodle soup with crispy fried pork cracklings arrives at their doorstep.",
            fullDescriptionZh = "跑堂少年手持两根竹条在深巷里清脆地敲击节奏。居民只需在门前打声招呼，几分钟后一碗热气腾腾、铺满薄切瘦肉与香脆油渣的美味汤面便会送至门前。",
            fullDescriptionJa = "竹の拍子木をリズミカルに鳴らしながら路地を歩き注文を取ります。声をかけると、数分後には熱々のフーティウにカリカリの豚脂かすとフライドエシャロットが乗って届きます。",
            fullDescriptionKo = "대나무 조각을 부딪치며 골목 구석구석을 누비면, 집 안에서 부르는 손님에게 갓 끓인 따끈한 국수를 배달해 줍니다. 바삭한 돼지 비계 튀김이 별미입니다.",
            whyItMattersVi = "Âm thanh ký ức: Bản hòa âm mộc mạc nuôi dưỡng tâm hồn và cứu đói những đêm làm việc muộn của bao thế hệ.",
            whyItMattersEn = "Acoustic heritage: The nocturnal soundtrack and comfort food that has sustained night workers and students for decades.",
            whyItMattersZh = "声音印记：承载着几代西贡人深夜加班、求学记忆的温暖市井交响乐与慰藉美食。",
            whyItMattersJa = "街の記憶：夜遅くまで働く人々や学生のお腹と心を満たしてきた、サイゴンの夜の風物詩です。",
            whyItMattersKo = "밤의 기억: 늦은 밤 일하는 이들과 학생들의 허기를 달래주던 사이공 골목의 정겨운 밤 풍경입니다.",
            triviaVi = "Âm thanh thanh tre gõ có tần số âm thanh vang rất xa qua các khúc cua hẹp của hẻm mà không gây chói tai.",
            triviaEn = "The resonant acoustic pitch of bamboo sticks cuts cleanly through maze walls without sounding overly harsh.",
            exampleLocationsVi = listOf("Hẻm 158 Pasteur (P. Sài Gòn)", "Hẻm 287 Nguyễn Đình Chiểu (P. Bàn Cờ)"),
            exampleLocationsEn = listOf("Alley 158 Pasteur (Saigon Ward)", "Alley 287 Nguyen Dinh Chieu (Ban Co Ward)")
        ),
        CulturalGlossaryItem(
            id = "tieng_rao",
            term = "Tiếng Rao Đêm & Ve Chai",
            phonetic = "[tiəŋ³⁵ zaːw³³] / Street Cries",
            toneGuide = "Thanh sắc - thanh ngang",
            category = GlossaryCategory.STREET_LIFE,
            icon = "📣",
            accentColor = Color(0xFFF59E0B),
            shortDefinitionVi = "Những giai điệu rao hàng rong mộc mạc: 'Ai bánh giò...', 'Ai ve chai bán hông...', linh hồn âm thanh hẻm phố.",
            shortDefinitionEn = "Melodic vendor street cries that form the acoustic soul and living rhythm of Saigon's alleyways.",
            shortDefinitionZh = "传统流动小贩悠扬质朴的叫卖声调，构成西贡小巷最鲜活的声音灵魂。",
            shortDefinitionJa = "「バーンヨーはいかが」「廃品回収〜」など、路地裏に響く物売りたちの情緒あふれる呼び声。",
            shortDefinitionKo = "'따끈한 떡 사세요', '고물 삽니다' 등 골목길에 울려 퍼지는 행상인들의 정겨운 노랫가락 소리.",
            fullDescriptionVi = "Mỗi món quà rong đều có một làn điệu rao riêng biệt. Từ tiếng rao bánh bao đêm, bánh giò nóng hổi đến tiếng chuông leng keng của bác bán kem que. Đây là di sản văn hóa phi vật thể sống động len lỏi qua từng nếp nhà.",
            fullDescriptionEn = "Every street snack carries its own vocal cadence and pitch contour. From the melancholic cries of hot steamed meat pies to the jingling bells of pedal ice-cream carts, these acoustic calls weave the living fabric of neighborhood life.",
            fullDescriptionZh = "每种小吃都有其独特的叫卖韵律。从深夜肉粽的悠长呼唤，到冰棒车清脆的铃铛声，这些声音是穿透岁月、流淌在每家每户门前的无形文化遗产。",
            fullDescriptionJa = "肉ちまき売りの哀愁ある節回しから、アイスキャンディー売りのベルの音まで、路地ごとに独特のサウンドスケープが息づいています。",
            fullDescriptionKo = "따뜻한 바오 빵부터 아이스크림 자전거의 딸랑거리는 종소리까지, 각 물품마다 고유한 억양과 가락이 있어 골목의 정취를 더해줍니다.",
            whyItMattersVi = "Cảm nhận chiều sâu: Giúp bạn lắng nghe nhịp thở của thành phố qua thính giác chứ không chỉ bằng thị giác.",
            whyItMattersEn = "Multi-sensory exploration: Encourages travelers to listen to the city's living acoustic layer alongside visual landmarks.",
            whyItMattersZh = "听觉沉浸：引导旅行者用耳朵倾听城市的呼吸与心跳，感受超越视觉的深层文化温度。",
            whyItMattersJa = "聴覚の旅：目で見える景色だけでなく、耳に届く音からも街の温もりを感じ取ることができます。",
            whyItMattersKo = "청각 탐방: 눈으로 보는 풍경을 넘어 귀로 전해지는 골목의 숨결과 따뜻한 정취를 음미할 수 있습니다.",
            triviaVi = "Ngày nay nhiều cô chú đã ghi âm tiếng rao vào loa phát thanh mini gắn sau xe đạp để đỡ tốn sức.",
            triviaEn = "Many modern hawkers now loop classic recordings through small bicycle-mounted megaphones.",
            exampleLocationsVi = listOf("Hẻm 287 Nguyễn Đình Chiểu (P. Bàn Cờ)", "Hẻm 118 Bùi Hữu Nghĩa (P. Chợ Lớn)"),
            exampleLocationsEn = listOf("Alley 287 Nguyen Dinh Chieu (Ban Co Ward)", "Alley 118 Bui Huu Nghia (Cho Lon Ward)")
        ),
        CulturalGlossaryItem(
            id = "ban_tho_than_tai",
            term = "Bàn Thờ Thần Tài - Thổ Địa",
            phonetic = "[tʰən³¹ taːj³¹ - tʰo³¹³ ɗiə³²]",
            toneGuide = "Thanh huyền - thanh hỏi - thanh nặng",
            category = GlossaryCategory.COMMUNITY,
            icon = "🕯️",
            accentColor = Color(0xFFEC4899),
            shortDefinitionVi = "Bàn thờ đặt sát sàn nhà ngay lối vào cửa hẻm, thể hiện tín ngưỡng cầu may mắn và bảo bọc đất đai.",
            shortDefinitionEn = "Ground-level twin shrines at every alley entrance honoring the Earth Spirit and God of Wealth.",
            shortDefinitionZh = "供奉在民宅与店铺地面的土地公与财神神龛，祈求庇佑一方平安与财源广进。",
            shortDefinitionJa = "各家の玄関先に地べたに置かれる土地公と財神の祭壇。商売繁盛と平穏を祈る信仰。",
            shortDefinitionKo = "문 앞 바닥에 모셔진 토지신과 재물신 제단으로, 가정의 평안과 번영을 기원하는 민간 신앙입니다.",
            fullDescriptionVi = "Khác với các bàn thờ tổ tiên đặt trên cao, bàn thờ Thổ Địa - Thần Tài luôn được đặt tiếp đất để gần gũi với long mạch đất đai. Người dân trong hẻm luôn dâng trà, hoa tươi, thuốc lá và chuối vào mỗi sáng sớm trước khi mở cửa làm ăn.",
            fullDescriptionEn = "Unlike ancestral altars elevated toward ceilings, the Earth God (Thổ Địa) and Wealth God (Thần Tài) altars sit right on the tiled floor to stay anchored to the earth. Neighbors offer fresh jasmine tea, fruit, and incense at dawn for prosperity.",
            fullDescriptionZh = "与高高在上的祖先神位不同，土地公与财神神龛必须紧贴地面，以接纳大地灵气。巷弄居民与店家每天清晨都会敬献清茶、香蕉与清香，祈求一天吉星高照。",
            fullDescriptionJa = "天井近くに祀られる先祖の祭壇とは異なり、大地の気に繋がるよう床の上に直接祀られます。毎朝お茶や線香、果物が供えられ、商売と暮らしの平穏が祈られます。",
            fullDescriptionKo = "높은 곳에 두는 조상 제단과 달리 대지의 기운과 맞닿도록 바닥에 놓습니다. 주민들은 매일 아침 차와 과일, 향을 올리며 하루의 안녕과 행운을 빕니다.",
            whyItMattersVi = "Tôn trọng tín ngưỡng: Tránh bước qua hay chắn trước mặt bàn thờ khi đi dạo và chụp ảnh trong hẻm.",
            whyItMattersEn = "Cultural respect: Never step over or obstruct these intimate ground-level shrines when framing photos.",
            whyItMattersZh = "民俗礼仪：在巷弄漫步与拍照时，请注意切勿跨越或踩踏地面的神龛以示尊重。",
            whyItMattersJa = "参拝マナー：足元にある神聖な祭壇です。写真を撮る際はまたいだり遮ったりしないよう注意しましょう。",
            whyItMattersKo = "문화적 존중: 골목을 걷거나 사진을 찍을 때 바닥의 제단을 밟거나 가리지 않도록 주의해야 합니다.",
            triviaVi = "Ông Địa bụng bự thường thích tỏi tươi, trong khi Thần Tài tay cầm thỏi vàng mang lại tài lộc.",
            triviaEn = "The cheerful pot-bellied Earth God is traditionally offered fresh garlic cloves to ward off bad energy.",
            exampleLocationsVi = listOf("Hẻm 206 Trần Hưng Đạo (P. Chợ Quán)", "Hẻm 144 Nguyễn Trãi (P. Cầu Ông Lãnh)"),
            exampleLocationsEn = listOf("Alley 206 Tran Hung Dao (Cho Quan Ward)", "Alley 144 Nguyen Trai (Cau Ong Lanh Ward)")
        ),
        CulturalGlossaryItem(
            id = "lang_nghe",
            term = "Làng Nghề Phố (Lồng Đèn Phú Bình)",
            phonetic = "[laːŋ³¹ ŋe³¹ fo³⁵]",
            toneGuide = "Thanh huyền - thanh huyền - thanh sắc",
            category = GlossaryCategory.COMMUNITY,
            icon = "🏮",
            accentColor = Color(0xFF14B8A6),
            shortDefinitionVi = "Những con hẻm làng nghề thủ công trăm năm ẩn mình giữa lòng phố thị hiện đại (đèn giấy kiếng, kim hoàn).",
            shortDefinitionEn = "Centenarian artisan enclaves preserved inside deep alleys (handcrafted cellophane lanterns, jewelers).",
            shortDefinitionZh = "隐藏于现代化都市深处的百年传统手工艺胡同（如富平玻璃纸提灯、金银加工街）。",
            shortDefinitionJa = "近代都市の奥深くに息づく、伝統工芸を受け継ぐ職人路地（セロハンランタン、宝飾細工など）。",
            shortDefinitionKo = "현대적 도심 속에 숨어 백 년 전통을 이어가는 장인 골목 (전통 셀로판 등불, 귀금속 세공 등).",
            fullDescriptionVi = "Tại Hẻm 47 Trịnh Đình Trọng (Phường Hòa Bình), các nghệ nhân vẫn giữ kỹ thuật chẻ tre, uốn khung, dán giấy kiếng màu đỏ và vẽ tay hoa văn rồng phượng truyền thống phục vụ Tết Trung Thu hàng năm.",
            fullDescriptionEn = "Inside Alley 47 Trinh Dinh Trong (Hoa Binh Ward), veteran master crafters split natural bamboo strips, shape intricate wire frames, and paste translucent ruby-red cellophane with hand-painted phoenix motifs for the Mid-Autumn Festival.",
            fullDescriptionZh = "在和平坊郑廷仲街47号巷内，老手艺人依然坚守着削竹蔑、扎骨架、贴透光红玻璃纸并手工绘制龙凤图腾的古老技艺，传承中秋节传统。",
            fullDescriptionJa = "ホアビン坊の路地では、職人たちが竹を削り、針金で骨組みを作り、透き通る赤いセロハン紙を貼って手描きで龍や鳳凰を描く中秋節ランタンの技を守り続けています。",
            fullDescriptionKo = "화빈동 47번지 골목에서는 장인들이 대나무를 깎고 프레임을 짜서 붉은 셀로판지를 붙이고 손수 용과 봉황을 그리는 전통 중추절 등불 제작 기법을 고수하고 있습니다.",
            whyItMattersVi = "Tôn vinh làng nghề: Cảm nhận sự bền bỉ của người nghệ nhân đô thị giữ lửa cho nét đẹp dân gian Việt Nam.",
            whyItMattersEn = "Honoring heritage: Connect with the dedication of urban artisans keeping traditional folk arts vibrant in modern times.",
            whyItMattersZh = "手艺传承敬意：带领探险家深入深巷，感受都市手艺人对传统民间艺术的坚守与匠心。",
            whyItMattersJa = "伝統工芸への敬意：路地の奥で消えゆく伝統工芸を守る職人たちの技と情熱に触れることができます。",
            whyItMattersKo = "장인 정신 가치: 골목 안에서 사라져가는 전통 민속 공예를 지켜나가는 장인들의 숨결을 느낄 수 있습니다.",
            triviaVi = "Giấy kiếng đỏ truyền thống tạo ra ánh sáng lung linh ấm áp khi thắp nến thật bên trong mà đèn pin điện tử không thể sánh được.",
            triviaEn = "Authentic cellophane captures candlelight with a warm nostalgic glow that plastic battery lights cannot match.",
            exampleLocationsVi = listOf("Hẻm 47 Trịnh Đình Trọng - Làng Lồng Đèn Phú Bình (P. Hòa Bình)", "Hẻm Thợ Kim Hoàn (P. Chợ Lớn)"),
            exampleLocationsEn = listOf("Alley 47 Trinh Dinh Trong - Phu Binh Lantern Village (Hoa Binh Ward)", "Jewelry Artisan Alley (Cho Lon Ward)")
        ),
        CulturalGlossaryItem(
            id = "sui_cao",
            term = "Sủi Cảo Hà Tôn Quyền",
            phonetic = "[suj³¹³ kaːw³¹³ haː³¹ ton³³ kwiəŋ³¹]",
            toneGuide = "Thanh ngã - thanh ngã - thanh huyền",
            category = GlossaryCategory.CULINARY,
            icon = "🥟",
            accentColor = Color(0xFFEF4444),
            shortDefinitionVi = "Con hẻm ẩm thực người Hoa danh tiếng chuyên món sủi cảo tôm tươi nguyên con bọc trong vỏ bột vàng óng.",
            shortDefinitionEn = "Famous Cantonese culinary alley renowned for handmade dumplings stuffed with whole succulent shrimp.",
            shortDefinitionZh = "闻名遐迩的堤岸粤式水饺一条街，以包裹整颗鲜嫩大虾的手工黄金水饺著称。",
            shortDefinitionJa = "大粒のエビが丸ごと入った手包み水餃子で名高い、チョロンの中華グルメストリート路地。",
            shortDefinitionKo = "탱글탱글한 통새우가 가득 찬 수제 만두로 유명한 쩌롱의 대표적인 딤섬 미식 골목.",
            fullDescriptionVi = "Hẻm 191 Hà Tôn Quyền tập trung hàng chục quán sủi cảo gia truyền qua 3 thế hệ. Sủi cảo ở đây có nhân tôm thịt nguyên con ngọt lịm, ăn kèm nước lèo nấu từ xương hầm và da heo giòn, mực ngâm tro và cải ngọt thanh mát.",
            fullDescriptionEn = "Alley 191 Ha Ton Quyen is packed with generational dumpling houses. Each plump wonton wrapper encloses whole whole shrimp and seasoned minced pork, served in a rich bone broth garnished with roasted pork skin, ash-soaked squid, and leafy greens.",
            fullDescriptionZh = "下宗权街191号巷聚集了数十家传承三代的水饺老铺。这里的鲜虾水饺颗颗饱满，配以猪骨慢炖的高汤、酥炸猪皮、爽脆鱿鱼以及清甜芥菜，鲜美绝伦。",
            fullDescriptionJa = "ハトンクエン路地には3世代続く水餃子の名店が並びます。エビが丸ごと入ったジューシーな水餃子を、豚骨スープや揚げ豚皮、イカとともにいただきます。",
            fullDescriptionKo = "하똔꾸옌 골목에는 3대째 이어져 온 만두 명가들이 모여 있습니다. 통새우가 씹히는 육즙 가득한 만두와 깊은 사골 육수, 바삭한 돼지 껍질의 조화가 일품입니다.",
            whyItMattersVi = "Ẩm thực chuẩn vị: Hướng dẫn bạn cách thưởng thức trọn vẹn tô sủi cảo thập cẩm hoặc sủi cảo chiên giòn rụm.",
            whyItMattersEn = "Culinary insider tip: Learn how to order like a local, choosing between mixed broth dumplings and golden crisp fried dumplings.",
            whyItMattersZh = "地道吃法指南：指导游人在探索著名的“下宗权水饺一条街”时，点选最地道的鲜虾水饺与特色脆皮炸饺。",
            whyItMattersJa = "絶品グルメの頼み方：スープ水餃子（Sủi Cảo Nước）とカリカリの揚げ餃子（Sủi Cảo Chiên）をローカル流に楽しめます。",
            whyItMattersKo = "미식 주문 팁: 맑은 국물의 물만두와 바삭한 군만두를 취향에 따라 현지인처럼 주문하는 법을 알려줍니다.",
            triviaVi = "Món sủi cảo thập cẩm tại đây đặc biệt có thêm 'mực ngâm tro' giòn sần sật theo bí quyết Quảng Đông cổ xưa.",
            triviaEn = "The signature mixed bowl features traditional Cantonese ash-cured squid for a distinct crispy snap.",
            exampleLocationsVi = listOf("Hẻm 191 Hà Tôn Quyền (P. Chợ Lớn)", "Hẻm 118 Bùi Hữu Nghĩa (P. Chợ Lớn)"),
            exampleLocationsEn = listOf("Alley 191 Ha Ton Quyen Dumpling Street (Cho Lon Ward)", "Alley 118 Bui Huu Nghia (Cho Lon Ward)")
        ),
        CulturalGlossaryItem(
            id = "com_tam",
            term = "Cơm Tấm Khói Than Hồng",
            phonetic = "[kəːm³³ təm³⁵]",
            toneGuide = "Thanh ngang - thanh sắc",
            category = GlossaryCategory.CULINARY,
            icon = "🥩",
            accentColor = Color(0xFFF97316),
            shortDefinitionVi = "Món ăn quốc hồn quốc túy Sài Gòn từ hạt gạo tấm thơm lừng nướng sườn than hồng ở lối vào hẻm phố.",
            shortDefinitionEn = "Saigon's soul food: Broken rice topped with marinated lemongrass pork chop grilled over live coals.",
            shortDefinitionZh = "西贡灵魂美食：在巷口炭火铁架上现烤的香茅排骨，配以松软独特的碎米饭。",
            shortDefinitionJa = "サイゴンのソウルフード。路地入口の炭火で香ばしく焼いた豚肉をのせた割れ米ご飯（コムタム）。",
            shortDefinitionKo = "사이공의 소울 푸드: 골목 입구 숯불에서 구워낸 레몬그라스 돼지갈비를 얹은 깨진 쌀 덮밥(껌승).",
            fullDescriptionVi = "Khởi nguồn từ những hạt gạo gãy khi xay xát dành cho người lao động, cơm tấm nay trở thành món ăn được yêu thích bậc nhất. Đĩa cơm tấm sườn bì chả chan nước mắm tỏi ớt kẹo và mỡ hành xanh mướt là hương vị đại diện cho nhịp sống Sài Gòn.",
            fullDescriptionEn = "Originally an economical dish made from broken grains sifted during milling for workers, broken rice has evolved into an iconic culinary legend. Served with char-grilled pork chops, steamed egg meatloaf, pickled daikon, and scallion oil.",
            fullDescriptionZh = "最初是碾米坊筛选出来的廉价碎米，专供码头工人果腹，如今已演变为全城狂热的国民美食。喷香的炭烤大排、肉皮丝、蒸蛋羹淋上葱油与甜辣鱼露，回味无穷。",
            fullDescriptionJa = "元々は製粉時に割れた砕け米を路地の労働者たちが炊いて食べたのが始まりです。現在では、香ばしい炭火焼き豚肉、蒸し卵、甘いヌックマムと味わうサイゴン名物となりました。",
            fullDescriptionKo = "원래는 방앗간에서 나온 부서진 쌀알을 골목 노동자들이 모아 밥을 지어 먹던 데서 유래했습니다. 숯불에 구운 돼지갈비, 계란 찜, 달콤한 생선 소스와 함께 사이공을 대표하는 음식이 되었습니다.",
            whyItMattersVi = "Ẩm thực bản địa: Nhận biết làn khói sườn nướng đặc trưng tỏa ra từ các lò than ở lối vào con hẻm.",
            whyItMattersEn = "Culinary tradition: Learn to spot the iconic white smoke and lemongrass aroma puffing from charcoal grills at alley entrances.",
            whyItMattersZh = "寻味口福：教会探险家凭借巷口炭火炉飘出的阵阵香茅排骨烟气，寻找最美味的巷弄碎米饭。",
            whyItMattersJa = "路地裏グルメの目印：路地の入口から立ち上る炭火焼きの煙と香茅の香りをたよりに、絶品コムタムを見つけられます。",
            whyItMattersKo = "골목 미식 탐방: 골목 입구에서 구수한 숯불 연기와 레몬그라스 향이 피어오르는 맛집을 찾아내는 법을 알려줍니다.",
            triviaVi = "Hạt gạo tấm hấp thu mỡ hành và nước mắm sánh đậm đà hơn nhiều so với hạt gạo nguyên vẹn thông thường.",
            triviaEn = "Because the grains are fractured, broken rice absorbs scallion oil and seasoned fish sauce far more intensely.",
            exampleLocationsVi = listOf("Cơm Tấm Hẻm 158 Pasteur (P. Sài Gòn)", "Cơm Tấm Hẻm 35 Ngô Thời Nhiệm (P. Xuân Hòa)"),
            exampleLocationsEn = listOf("Alley Broken Rice 158 Pasteur (Saigon Ward)", "Alley Broken Rice 35 Ngo Thoi Nhiem (Xuan Hoa Ward)")
        ),
        CulturalGlossaryItem(
            id = "tra_da_via_he",
            term = "Trà Đá Vỉa Hè & Thùng Nước Miễn Phí",
            phonetic = "[caː³¹ ɗaː³⁵ viə³¹³ hɛ³¹]",
            toneGuide = "Thanh huyền - thanh sắc - thanh ngã - thanh huyền",
            category = GlossaryCategory.COMMUNITY,
            icon = "🧊",
            accentColor = Color(0xFF0EA5E9),
            shortDefinitionVi = "Những thùng trà đá miễn phí đặt trước đầu hẻm - biểu tượng cho lòng hào sảng, tương thân tương ái của người Sài Gòn.",
            shortDefinitionEn = "Free iced tea containers placed at alley mouths: Saigon's enduring symbol of street hospitality and generosity.",
            shortDefinitionZh = "摆放在巷口的免费冰茶桶，体现了西贡人热情豪爽、互帮互助的温情城市精神。",
            shortDefinitionJa = "路地の入口に置かれる「無料のお茶タンク」。サイゴン市民の温かいもてなしとお互い様の精神の象徴。",
            shortDefinitionKo = "골목 입구마다 놓인 무료 얼음차 물통으로, 사이공 사람들의 넉넉한 인심과 나눔의 정을 보여줍니다.",
            fullDescriptionVi = "Dưới cái nắng nhiệt đới quanh năm, các gia đình đầu hẻm thường tự nấu trà hoa lài, châm đá mát lạnh vào những thùng inox có ghi chữ 'Trà đá miễn phí' cho người lao động, người đi đường giải khát. Nét đẹp bình dị này không cần đền đáp.",
            fullDescriptionEn = "Under the tropical heat, alleyway households brew giant urns of chilled jasmine tea labeled 'Free Iced Tea' for couriers, street cleaners, and thirsty pedestrians. It reflects the pure, unprompted generosity woven into the city's soul.",
            fullDescriptionZh = "在终年炎热的热带阳光下，巷口的居民常自发煮制大桶茉莉冰茶，贴上“免费冰茶”字样，供过路行人和户外劳动者消暑解渴。这种不求回报的善举温暖着整座城市。",
            fullDescriptionJa = "暑い日差しの中、路地の住民たちが大きなタンクに冷たいジャスミン茶を用意し、配達員や通行人に無料で振る舞っています。見返りを求めない思いやりが街に溢れています。",
            fullDescriptionKo = "뜨거운 햇살 아래, 골목 주민들이 자발적으로 시원한 자스민 차를 끓여 지나가는 배달원과 노동자들에게 무료로 제공합니다. 대가 없는 따뜻한 온정을 느낄 수 있습니다.",
            whyItMattersVi = "Tâm hồn Sài Gòn: Hiểu được triết lý sống nghĩa hiệp, bao dung và sẻ chia của người dân phương Nam.",
            whyItMattersEn = "Soul of the city: Experience the heartfelt philosophy of kindness and community sharing that defines Southern Vietnam.",
            whyItMattersZh = "人文温度：感受越南南部百姓宽厚包容、乐善好施、守望相助的精神风貌。",
            whyItMattersJa = "心のふれあい：見知らぬ人同士でも助け合う、南部ベトナムの温かい精神性に触れることができます。",
            whyItMattersKo = "사이공의 인심: 낯선 이에게도 아낌없이 베푸는 남부 베트남 특유의 따뜻하고 관대한 공동체 문화를 배웁니다.",
            triviaVi = "Nhiều bình nước miễn phí còn có kèm theo tủ thuốc y tế gia đình nhỏ gắn trên tường hẻm.",
            triviaEn = "Some alley stations also feature complimentary roadside first-aid kits and emergency tire pump stations.",
            exampleLocationsVi = listOf("Hẻm 158 Pasteur (P. Sài Gòn)", "Hẻm 287 Nguyễn Đình Chiểu (P. Bàn Cờ)", "Hẻm 206 Trần Hưng Đạo (P. Chợ Quán)"),
            exampleLocationsEn = listOf("Alley 158 Pasteur (Saigon Ward)", "Alley 287 Nguyen Dinh Chieu (Ban Co Ward)", "Alley 206 Tran Hung Dao (Cho Quan Ward)")
        ),
        CulturalGlossaryItem(
            id = "van_hoa_sinh_vien_bk",
            term = "Hẻm Sinh Viên Bách Khoa & Quán Cóc Đồ Án",
            phonetic = "[hɛm³¹³ ʂïŋ¹ viən¹ ɓaʔk⁴⁵ xoə¹]",
            toneGuide = "Thanh hỏi - thanh ngang - thanh ngang - thanh sắc - thanh ngang",
            category = GlossaryCategory.COMMUNITY,
            icon = "🎓",
            accentColor = Color(0xFF2563EB),
            shortDefinitionVi = "Hệ sinh thái hẻm quanh ĐH Bách Khoa: tụ điểm ẩm thực vỉa hè giá rẻ, quán cà phê 24/7 và nơi thắp lửa đồ án.",
            shortDefinitionEn = "The student alley ecosystem around HCMUT: affordable street eats, 24/7 maker cafes, and engineering project hubs.",
            shortDefinitionZh = "胡志明市理工大学周边的学生巷弄生态：平价街头美食、24小时创客咖啡馆与科研毕业设计聚集地。",
            shortDefinitionJa = "ホーチミン工科大学周辺の学生街ヘム。格安グルメ、24時間スタディカフェ、熱気あふれる工学系コミュニティ。",
            shortDefinitionKo = "호치민 공과대학교 주변의 학생 골목 생태계: 저렴한 길거리 음식, 24시간 스터디 카페, 청춘의 프로젝트 공간.",
            fullDescriptionVi = "Bao quanh khuôn viên ĐH Bách Khoa (khu vực Tô Hiến Thành, Lý Thường Kiệt, Lữ Gia, Hòa Hảo) là những con hẻm sôi động, nơi sinh viên các thế hệ gắn bó với từng dĩa cơm tấm sinh viên, ly trà đá vỉa hè và những đêm thức trắng lập trình, thiết kế đồ án.",
            fullDescriptionEn = "Surrounding the prestigious HCMUT campus (To Hien Thanh, Ly Thuong Kiet, Lu Gia, Hoa Hao) is a vibrant alley network where generations of engineering students share budget-friendly street food, roadside iced tea, and all-nighter coding/drafting sessions.",
            fullDescriptionZh = "围绕胡志明市理工大学校园（苏宪成路、李常杰路、吕嘉路、和平路）的小巷充满了勃勃生机，见证了数代工程学子品尝平价美食、畅饮路边冰茶并熬夜奋战科研项目。",
            fullDescriptionJa = "工科大学の周りには活気に満ちた路地が広がり、安くて美味しい学食風屋台や、夜通し設計やプログラミングに打ち込む学生たちの青春が詰まっています。",
            fullDescriptionKo = "공과대학 주변의 활기찬 골목길에는 저렴한 학생 식당과 노천 카페, 밤샘 프로젝트에 몰두하는 학생들의 젊음과 열정이 고스란히 묻어있습니다.",
            whyItMattersVi = "Nhịp đập tri thức & thanh xuân: Trải nghiệm không khí học thuật kết hợp đời sống sinh viên bình dị nhưng nhiệt huyết tại Sài Gòn.",
            whyItMattersEn = "Pulse of youth and innovation: Experience the energetic academic lifestyle and genuine warmth of Saigon's future engineers.",
            whyItMattersZh = "青春与智慧的脉动：感受年轻学子的学术活力与西贡接地气而又充满梦想的大学城文化。",
            whyItMattersJa = "若さと知性の鼓動：未来のエンジニアたちが集う、サイゴンの活気ある大学街の素顔に触れられます。",
            whyItMattersKo = "젊음과 지성의 숨결: 미래의 공학도들이 꿈을 키워가는 사이공 특유의 정겹고 열정적인 대학가 문화를 경험합니다.",
            triviaVi = "Khu vực Lữ Gia và Tô Hiến Thành tập trung nhiều tiệm in đồ án và tiệm linh kiện điện tử nhất khu vực Q.10.",
            triviaEn = "Lu Gia and To Hien Thanh alleys host Saigon's highest density of 24-hour architectural blueprint printers and DIY electronics shops.",
            exampleLocationsVi = listOf("Hẻm 493 Tô Hiến Thành (P. Diên Hồng, Q.10)", "Cư Xá Lữ Gia (P.15, Q.11 / Giáp BK)", "Ký Túc Xá ĐH Bách Khoa (497 Hòa Hảo, P.7, Q.10)"),
            exampleLocationsEn = listOf("Alley 493 To Hien Thanh (District 10)", "Lu Gia Maker Cafe Hub (District 10)", "HCMUT Student Dormitory (497 Hoa Hao)")
        ),
        CulturalGlossaryItem(
            id = "bk_com_tam_tang_ca",
            term = "Cơm Tấm Sinh Viên & Đĩa Cơm Tăng Ca Đồ Án",
            phonetic = "[kəm¹ təm⁴⁵ ʂïŋ¹ viən¹]",
            toneGuide = "Thanh ngang - thanh sắc - thanh ngang - thanh ngang",
            category = GlossaryCategory.CULINARY,
            icon = "🍛",
            accentColor = Color(0xFFEA580C),
            shortDefinitionVi = "Những đĩa cơm tấm đầy đặn giá sinh viên, tiếp năng lượng cho các kỹ sư tương lai thức thâu đêm làm đồ án.",
            shortDefinitionEn = "Hearty, student-priced broken rice plates fueling future engineers through marathon drafting & coding deadlines.",
            shortDefinitionZh = "分量十足、价格亲民的学生碎米饭，为挑灯夜战完成科研设计的未来工程师们注入满满能量。",
            shortDefinitionJa = "安くて大盛りの学生向けコムタム（砕き米ご飯）。設計や卒論の徹夜作業を支えるエンジニアたちのソウルフード。",
            shortDefinitionKo = "푸짐한 양과 착한 가격의 학생 껌땀(깨진 쌀밥). 밤샘 과제에 매진하는 공대생들의 든든한 에너지원.",
            fullDescriptionVi = "Xung quanh Cổng 3 ĐH Bách Khoa và hẻm Tô Hiến Thành, các quán cơm tấm mở từ trưa đến tận nửa đêm với giá chỉ từ 25.000 - 35.000đ. Đĩa cơm luôn hào phóng thêm cơm, chén canh nóng và nước mắm chua ngọt đậm đà, là vị cứu tinh của hàng ngàn sinh viên mỗi mùa bảo vệ đồ án.",
            fullDescriptionEn = "Scattered near Gate 3 of HCMUT and To Hien Thanh alleyways, student broken rice joints run until midnight with extra-generous rice servings, complimentary piping-hot soup, and rich sweet-sour fish sauce. They have fueled generations of students during grueling thesis defense weeks.",
            fullDescriptionZh = "分布在胡志明市理工大学三号门和苏宪成路小巷的碎米饭摊点营业至深夜，价格仅2.5万至3.5万越盾。老板总是慷慨地给学生免费加饭添汤，是学子们答辩季最温暖的后盾。",
            fullDescriptionJa = "工科大3号門やトーヒエンタイン路地沿いのコムタム屋は深夜まで営業。ご飯のおかわり無料で温かいスープが付き、卒業設計シーズンの学生たちの強い味方です。",
            fullDescriptionKo = "공과대 3번 게이트와 토히엔탄 골목 일대 껌땀 식당들은 푸짐한 무료 밥 리필과 따뜻한 국물로 졸업 프로젝트를 준비하는 학생들을 든든하게 지켜줍니다.",
            whyItMattersVi = "Ẩm thực nuôi dưỡng ước mơ: Hiểu được đời sống sinh viên chân thực, nơi sự hào sảng của chủ quán chia sẻ gánh nặng với người trẻ lập nghiệp.",
            whyItMattersEn = "Food nurturing dreams: Witness genuine student life where street vendors' generosity actively supports young minds building their future.",
            whyItMattersZh = "梦想的滋养：感受最接地气的西贡市井温情，体会摊贩老板对拼搏青年学子的体贴与呵护。",
            whyItMattersJa = "夢を育む食文化：若者の挑戦を温かく見守り応援する、サイゴンの下町ならではの人情に触れられます。",
            whyItMattersKo = "꿈을 키우는 밥상: 미래를 위해 치열하게 도전하는 청년들을 따뜻하게 품어주는 사이공 골목의 넉넉한 인심.",
            triviaVi = "Nhiều quán cơm quanh Bách Khoa có truyền thống 'sinh viên bao no' - cho phép thêm cơm và canh hoàn toàn miễn phí.",
            triviaEn = "Many diners around HCMUT maintain the long-standing tradition of free unlimited rice and soup refills for students.",
            exampleLocationsVi = listOf("Hẻm 493 Tô Hiến Thành (P. Diên Hồng, Q.10)", "Cổng 3 ĐH Bách Khoa (Đường 3/2)", "Hẻm 284 Lý Thường Kiệt (P.14, Q.10)"),
            exampleLocationsEn = listOf("Alley 493 To Hien Thanh (District 10)", "HCMUT Gate 3 (3/2 Street)", "Alley 284 Ly Thuong Kiet (District 10)")
        ),
        CulturalGlossaryItem(
            id = "bk_ca_phe_thuc_247",
            term = "Cà Phê Thức 24/7 & Hẻm Cư Xá Lữ Gia",
            phonetic = "[kaː²¹ feː¹ tʰɯk⁴⁵]",
            toneGuide = "Thanh huyền - thanh ngang - thanh sắc",
            category = GlossaryCategory.COMMUNITY,
            icon = "☕",
            accentColor = Color(0xFF0D9488),
            shortDefinitionVi = "Những quán cà phê thâu đêm quanh Bách Khoa nơi rực sáng màn hình laptop, tiếng gõ phím và bản vẽ kỹ thuật.",
            shortDefinitionEn = "24/7 study cafes surrounding HCMUT glowing with laptop screens, mechanical keyboard clatter, and engineering blueprints.",
            shortDefinitionZh = "理工大学周边的24小时通宵自习咖啡馆，屏幕荧光闪烁，机械键盘声与工程CAD图纸交织成独特的学术夜景。",
            shortDefinitionJa = "工科大周辺の24時間営業カフェ。夜通しパソコンの明かりが灯り、図面作成やプログラミングに熱中する学生たちの拠点。",
            shortDefinitionKo = "공대 주변의 24시간 스터디 카페. 밤새 빛나는 노트북 화면과 키보드 소리, 도면이 어우러진 학구열 넘치는 공간.",
            fullDescriptionVi = "Cư Xá Lữ Gia và các con hẻm lân cận ĐH Bách Khoa là thủ phủ của các quán cà phê mở thâu đêm suốt sáng. Với ổ cắm điện dày đặc, wifi tốc độ cao và ly bạc xỉu đá đậm vị, đây là nơi ấp ủ hàng ngàn đồ án robot, công trình xây dựng và startup công nghệ của sinh viên Bách Khoa.",
            fullDescriptionEn = "Lu Gia Residential Quarter and adjoining alleyways are Saigon's capital of 24/7 maker and study cafes. Equipped with wall-to-wall power outlets, high-speed fiber internet, and potent iced condensed milk coffees, they host late-night robotics builds, architectural rendering marathons, and student tech ventures.",
            fullDescriptionZh = "吕嘉居舍及周边小巷是西贡通宵创客与自习咖啡馆的大本营。密集的电源插座、高速网络与香浓的三色白咖啡，见证了无数机器人项目、建筑渲染与科技初创团队的诞生。",
            fullDescriptionJa = "ルージア地区の路地裏には24時間カフェが集結。十分な電源と高速Wi-Fi、濃厚な練乳コーヒーをお供に、学生たちがロボット制作や建築コンペのアイデアを練り上げています。",
            fullDescriptionKo = "르자 주택가 골목은 24시간 스터디 카페의 메카입니다. 넉넉한 콘센트와 빠른 와이파이, 진한 연유 커피와 함께 로봇 제작과 건축 렌더링에 몰두하는 학생들을 만날 수 있습니다.",
            whyItMattersVi = "Văn hóa sáng tạo trẻ: Chứng kiến tinh thần nỗ lực không ngừng nghỉ và khát vọng công nghệ của thế hệ trẻ Việt Nam.",
            whyItMattersEn = "Young Innovation Culture: Witness the relentless work ethic, intellectual drive, and tech ambitions of Vietnam's next generation.",
            whyItMattersZh = "青年创新活力：亲身感受越南青年一代勤勉刻苦、追求卓越的科技创造力与创业精神。",
            whyItMattersJa = "若者のイノベーション文化：昼夜を問わず情熱を注ぐ、ベトナムの若きエンジニアたちの探求心に出会えます。",
            whyItMattersKo = "청년 혁신 문화: 밤낮없이 미래를 개척해 나가는 베트남 차세대 공학도들의 끊임없는 열정과 도전 정신.",
            triviaVi = "Mỗi mùa thi cuối kỳ, nhiều quán cà phê ở Cư Xá Lữ Gia kín chỗ 24/24 và phục vụ trà đá miễn phí liên tục.",
            triviaEn = "During final exam and thesis weeks, Lu Gia cafes operate at full 24-hour capacity with complimentary iced tea refills.",
            exampleLocationsVi = listOf("Đường Số 3, Cư Xá Lữ Gia (P.15, Q.11)", "Hẻm 493 Tô Hiến Thành (P. Diên Hồng, Q.10)", "Hẻm 142 Tô Hiến Thành (P. Diên Hồng, Q.10)"),
            exampleLocationsEn = listOf("Street No. 3, Lu Gia (District 11)", "Alley 493 To Hien Thanh (District 10)", "Alley 142 To Hien Thanh (District 10)")
        ),
        CulturalGlossaryItem(
            id = "bk_pho_in_an_nhat_tao",
            term = "Phố In Bản Vẽ & Chợ Linh Kiện Nhật Tảo - BK",
            phonetic = "[fǒː³¹³ ʔïn¹ ɓaːn³¹³ vɛʔ⁴⁵]",
            toneGuide = "Thanh phố - thanh ngang - thanh hỏi - thanh ngã",
            category = GlossaryCategory.COMMUNITY,
            icon = "📐",
            accentColor = Color(0xFF4F46E5),
            shortDefinitionVi = "Tuyến phố in ấn bản vẽ khổ lớn A0 và chợ linh kiện điện tử lớn nhất phương Nam sát cạnh ĐH Bách Khoa.",
            shortDefinitionEn = "The high-speed A0 blueprint printing hub and Southern Vietnam's largest electronics bazaar adjacent to HCMUT.",
            shortDefinitionZh = "紧邻理工大学的大幅面A0工程图纸打印集聚区与越南南方最大的日藻电子元器件元老级大市场。",
            shortDefinitionJa = "工科大隣接のA0大判図面印刷ストリート＆南部ベトナム最大のニャッチャオ電子部品・自作パーツ市場。",
            shortDefinitionKo = "공대 인근의 A0 대형 도면 출력 전문 거리와 베트남 남부 최대의 녓따오 전자 부품 DIY 상가.",
            fullDescriptionVi = "Chỉ cách ĐH Bách Khoa vài phút đi bộ là Chợ Nhật Tảo và dãy phố in ấn Lữ Gia. Nơi đây cung cấp mọi thứ từ vi điều khiển, cảm biến, chip vi xử lý đến các cuộn bản vẽ kiến trúc khổ lớn. Tiếng máy in chạy rào rào và những khay linh kiện là thế giới diệu kỳ của dân kỹ thuật.",
            fullDescriptionEn = "A short walk from campus leads to Nhat Tao Electronics Bazaar and the Lu Gia blueprint printing row. Supplying everything from microcontrollers, IoT sensors, and solder kits to massive architectural plotting rolls, this is the ultimate playground for makers and tech creators.",
            fullDescriptionZh = "步行数分钟即可抵达日藻电子商圈与吕嘉图纸打印一条街。从微控制器、各类传感器、芯片焊接到大型建筑蓝图应有尽有，是理工创客们的天堂。",
            fullDescriptionJa = "大学から徒歩圏内に広がる電子部品街と図面印刷街。マイコンボードや各種センサーから特大図面まで揃う、ものづくり愛好家の聖地。",
            fullDescriptionKo = "캠퍼스에서 도보 거리에 있는 녓따오 전자상가와 도면 출력 거리. 마이크로컨트롤러, 센서부터 대형 건축 도면까지 모든 것이 갖춰진 메이커들의 성지.",
            whyItMattersVi = "Hậu cần tri thức: Nơi biến các ý tưởng lý thuyết trên giảng đường thành sản phẩm phần cứng thực tế.",
            whyItMattersEn = "Knowledge logistics: The tangible ecosystem that transforms classroom theory into working physical hardware innovations.",
            whyItMattersZh = "知识成果转化摇篮：见证理论构想在此一步步转化为真实的工业原型与创新硬件产品。",
            whyItMattersJa = "知識の具現化の場：机上の理論を実際のハードウェアやプロトタイプへと形にする技術の街です。",
            whyItMattersKo = "지식의 실현 창구: 강의실에서 배운 이론이 실제 하드웨어와 프로토타입으로 완성되는 생생한 기술 현장.",
            triviaVi = "Chợ Nhật Tảo được thành lập từ thập niên 1980 và là nơi mọi kỹ sư cơ điện tử, tự động hóa Sài Gòn đều từng ghé qua mua linh kiện.",
            triviaEn = "Founded in the 1980s, Nhat Tao Market is the rite of passage for every robotics and hardware engineer in Ho Chi Minh City.",
            exampleLocationsVi = listOf("Chợ Điện Tử Nhật Tảo (Đường Nguyễn Kim / Nhật Tảo, Q.10)", "Dãy in ấn Cư Xá Lữ Gia (P.15, Q.11)", "Hẻm 268 Lý Thường Kiệt (P. Diên Hồng, Q.10)"),
            exampleLocationsEn = listOf("Nhat Tao Electronics Market (District 10)", "Lu Gia Blueprint Row (District 11)", "Alley 268 Ly Thuong Kiet (District 10)")
        ),
        CulturalGlossaryItem(
            id = "bk_tra_da_san_co",
            term = "Cóc Trà Đá Vỉa Hè & Sân Cờ Cổng Trường BK",
            phonetic = "[tɕaː²¹ ɗaː⁴⁵ vaː²¹ hɛ²¹]",
            toneGuide = "Thanh huyền - thanh sắc - thanh huyền - thanh huyền",
            category = GlossaryCategory.COMMUNITY,
            icon = "♟️",
            accentColor = Color(0xFF16A34A),
            shortDefinitionVi = "Những bàn trà đá cóc mát lạnh trước cổng trường, nơi sinh viên đấu cờ tướng và tranh luận khoa học.",
            shortDefinitionEn = "Breezy roadside iced-tea stalls at campus gates where students play Chinese chess and debate scientific theories.",
            shortDefinitionZh = "校门口树荫下的路边微风冰茶小摊，学子们在此下象棋、探讨学术难题并畅谈人生志向。",
            shortDefinitionJa = "正門前の木陰に並ぶローカルな路上アイスティー席。将棋を指したり工学の議論を交わす学生の憩いの場。",
            shortDefinitionKo = "교문 앞 가로수 그늘 아래 노천 차 좌석. 장기를 두고 학문적 토론을 나누는 학생들의 정겨운 쉼터.",
            fullDescriptionVi = "Dưới bóng mát của những cây cổ thụ trước Cổng 1 (Lý Thường Kiệt) và Cổng 3 (Tô Hiến Thành), những chiếc ghế nhựa con con cùng ly trà đá 3.000đ là không gian giải lao kinh điển. Giữa tiếng ve râm ran, sinh viên giải đề thi, tranh luận thuật toán và thách đấu cờ thế cùng các bác xe ôm thân thiện.",
            fullDescriptionEn = "Under ancient shady trees at Campus Gate 1 and Gate 3, low plastic stools and 3,000 VND iced teas form the quintessential campus hangout. Amid chirping cicadas, students debug coding logic, debate physics, and challenge friendly neighborhood cyclo drivers to fast chess matches.",
            fullDescriptionZh = "在李常杰路正门与苏宪成路侧门的参天大树下，几张低矮塑料凳配上一杯3000越盾的清凉冰茶，便是最具情怀的课间聚集地。大家在此推演算法、切磋棋艺，气氛融洽而热烈。",
            fullDescriptionJa = "大樹の木陰で小さなプラスチック椅子に腰掛け、冷たいお茶を飲みながらチェスや難問の解法を語り合うサイゴン工科大の伝統風景。",
            fullDescriptionKo = "캠퍼스 정문 가로수 그늘 아래 옹기종기 모여 앉아 시원한 차 한 잔과 함께 코딩 알고리즘을 토론하고 장기를 두는 풍경.",
            whyItMattersVi = "Nét bình dị học đường: Không gian cởi mở kết nối sinh viên với người dân lao động trong tình cảm xóm giềng ấm áp.",
            whyItMattersEn = "Campusside simplicity: An egalitarian, open-air space bridging aspiring intellectuals with hardworking everyday citizens.",
            whyItMattersZh = "平民学府温情：开放包容的市井空间，拉近了青年知识分子与普通劳动大众之间的真挚情谊。",
            whyItMattersJa = "キャンパスの素朴な温もり：学生たちと地域の庶民が気さくに交流する、開かれた下町空間です。",
            whyItMattersKo = "소박한 캠퍼스 정서: 청년 지식인들과 이웃 주민들이 격의 없이 어우러지는 따뜻하고 열린 커뮤니티.",
            triviaVi = "Mỗi bàn trà đá thường có sẵn bàn cờ tướng vẽ trực tiếp lên mặt bàn gỗ hoặc ghế nhựa để người chơi tùy nghi so tài.",
            triviaEn = "Many tea stalls feature permanent chessboards hand-painted directly onto wooden tables or plastic stool tops.",
            exampleLocationsVi = listOf("Cổng 1 ĐH Bách Khoa (268 Lý Thường Kiệt)", "Cổng 3 ĐH Bách Khoa (Đường Tô Hiến Thành)", "Hẻm 493 Tô Hiến Thành (P. Diên Hồng, Q.10)"),
            exampleLocationsEn = listOf("HCMUT Gate 1 (268 Ly Thuong Kiet)", "HCMUT Gate 3 (To Hien Thanh St)", "Alley 493 To Hien Thanh (District 10)")
        ),
        CulturalGlossaryItem(
            id = "bac_xiu",
            term = "Bạc Xỉu (Bạc Tẩy Xỉu Phé)",
            phonetic = "[ɓaːk̚³² siw³¹³]",
            toneGuide = "Bạc (nặng) - Xỉu (hỏi/ngã Nam Bộ)",
            category = GlossaryCategory.CULINARY,
            icon = "🥛",
            accentColor = Color(0xFFF59E0B),
            shortDefinitionVi = "Thức uống sữa nóng điểm chút cà phê thơm do người Hoa Chợ Lớn sáng tạo, nay là biểu tượng văn hóa Sài Gòn.",
            shortDefinitionEn = "Hot sweet milk infused with a delicate drop of drip coffee, invented by Cantonese locals in Cho Lon.",
            shortDefinitionZh = "由堤岸华人华侨独创的白奶滴咖啡（白啡），已成为西贡最具代表性的晨饮文化符号。",
            shortDefinitionJa = "熱いミルクに少量の濃縮珈琲を垂らした、チョロンの中華系住民発祥の伝統的スイートドリンク。",
            shortDefinitionKo = "따뜻한 연유 우유에 에스프레소 방울을 살짝 더한 음료로, 초롱 화교 커뮤니티에서 유래된 사이공의 상징.",
            fullDescriptionVi = "Bạc xỉu bắt nguồn từ tiếng Quảng Đông 'Bạc tẩy xỉu phé' (bạc: trắng/sữa, tẩy: ly, xỉu: một chút, phé: cà phê). Ban đầu phục vụ phụ nữ và trẻ em không quen uống cà phê đen đắng, bạc xỉu dần trở thành thức uống thanh xuân của mọi thế hệ người Sài Gòn.",
            fullDescriptionEn = "Originating from the Cantonese phrase 'Bạc Tẩy Xỉu Phé' (White cup with a little coffee), this beverage was created for women and youths who found robusta coffee too bitter. Today, it represents the soulful multicultural fusion of Saigon alley life.",
            fullDescriptionZh = "源自粤语“白底微啡”，最初为喝不惯浓苦黑咖啡的妇女和孩童调制。如今演变成西贡深巷茶档与清晨最温馨甜蜜的集体记忆。",
            fullDescriptionJa = "広東語の「白底少啡」に由来し、苦いコーヒーが苦手な人向けに作られたのが始まり。今やサイゴンのカフェ文化に欠かせない一杯です。",
            fullDescriptionKo = "광둥어 '백저소비'에서 유래하여 쓰지 않고 부드러운 맛을 즐기도록 고안되었으며, 현재는 사이공 골목길 아침을 여는 대표적인 달콤한 음료입니다.",
            whyItMattersVi = "Minh chứng cho sự giao thoa văn hóa ẩm thực Hoa - Việt đặc trưng của hẻm phố Sài Gòn.",
            whyItMattersEn = "Living proof of the seamless Vietnamese-Chinese culinary fusion that defines Saigon's neighborhood fabric.",
            whyItMattersZh = "展现了西贡深巷中越华文化交融的独特包容力与饮食智慧。",
            whyItMattersJa = "ベトナムと中華の食文化が見事に融合した、サイゴン路地裏の寛容な歴史を物語る一杯です。",
            whyItMattersKo = "베트남과 화교 문화가 정답게 어우러진 사이공 골목길의 따뜻한 융합 문화를 보여줍니다.",
            triviaVi = "Bạc xỉu chuẩn vị xưa của các quán Chợ Lớn luôn được uống khi còn nóng hổi cùng bánh tiêu hoặc giò cháo quẩy giòn.",
            triviaEn = "Authentic old-school Bac Xiu in Cho Lon is traditionally enjoyed steaming hot, paired with fried Chinese crullers.",
            exampleLocationsVi = listOf("Quán Cà Phê Vợt Ba Lù (Chợ Phùng Hưng, Q.5)", "Cà Phê Cheo Leo (Hẻm 109 Nguyễn Thiện Thuật, Q.3)", "Hẻm 493 Tô Hiến Thành, Q.10"),
            exampleLocationsEn = listOf("Ba Lu Sock Coffee (Phung Hung Market, D5)", "Cheo Leo Sock Coffee (Alley 109 Nguyen Thien Thuat, D3)", "Alley 493 To Hien Thanh (D10)")
        ),
        CulturalGlossaryItem(
            id = "gach_bong_xua",
            term = "Gạch Bông & Gạch Hoa Cổ Điển",
            phonetic = "[ɣajk̚³⁵ ɓawŋm³³]",
            toneGuide = "Thanh sắc - thanh ngang",
            category = GlossaryCategory.ARCHITECTURE,
            icon = "🟩",
            accentColor = Color(0xFF0D9488),
            shortDefinitionVi = "Những viên gạch xi măng ép hoa văn thủ công tinh xảo trên sàn nhà biệt thự Pháp và hành lang cư xá cổ.",
            shortDefinitionEn = "Handmade encaustic cement floor tiles adorned with geometric floral motifs found in vintage villas and modernist Cư Xá.",
            shortDefinitionZh = "经典手工压制彩色水泥印花地砖，见于法属殖民老宅、老式排屋及20世纪中叶现代主义老公寓中。",
            shortDefinitionJa = "手作りのエンカウスティック・セメントタイル。古い洋館やレトロな集合住宅の廊下を彩る美しい幾何学模様。",
            shortDefinitionKo = "식민지 시절 빌라와 옛 아파트 복도를 장식한 수제 엔코스틱 시멘트 플로어 타일.",
            fullDescriptionVi = "Du nhập từ Pháp vào cuối thế kỷ 19, gạch bông xi măng nhanh chóng trở thành linh hồn thẩm mỹ của kiến trúc nhà ở Sài Gòn. Mỗi viên gạch được thợ thủ công đổ từng lớp bột màu khoáng chất vào khuôn kim loại rồi ép thủy lực, tạo nên bề mặt bóng mát lạnh dưới chân người đi bộ.",
            fullDescriptionEn = "Introduced from France in the late 19th century, cement tiles became the aesthetic soul of Saigon residential spaces. Each tile is manually poured with natural mineral pigments and hydraulically pressed, keeping floors naturally cool in tropical heat.",
            fullDescriptionZh = "自19世纪末引入越南后，花砖成为西贡住宅美学的灵魂。匠人将天然矿物颜料倒入铜模，经高压压制而成，在热带气候下踏之清凉怡人。",
            fullDescriptionJa = "19世紀末にフランスから伝わり、サイゴンの住宅美学の象徴となりました。職人が顔料を型に流し込みプレスしたタイルは、熱帯の気候でも足元を涼しく保ちます。",
            fullDescriptionKo = "19세기 후반에 도입되어 사이공 주택의 시각적 상징이 된 수제 타일. 천연 광물 안료를 유압 프레스로 압축 제작하여 열대 기후에도 시원한 촉감을 선사합니다.",
            whyItMattersVi = "Di sản thị giác: Tạo nên vẻ đẹp hoài niệm, vượt thời gian khi khám phá các ngách sâu và quán cà phê ẩn mình.",
            whyItMattersEn = "Visual heritage: Adds evocative timeless beauty when discovering hidden alley lofts and heritage cafes.",
            whyItMattersZh = "视觉遗产：漫步深巷古楼与隐秘艺术空间时，脚下的花砖是跨越时空的艺术诗篇。",
            whyItMattersJa = "視覚的遺産：路地の奥に佇むリノベーションカフェや古アパートで、ノスタルジックな美しさを放ちます。",
            whyItMattersKo = "시각적 유산: 골목길 히든 카페와 오래된 건물을 탐방할 때 과거로 시간 여행을 떠난 듯한 감성을 전해줍니다.",
            triviaVi = "Gạch bông càng dùng lâu năm và lau bằng nước sạch thì màu men càng sáng bóng và đượm màu thời gian.",
            triviaEn = "Encaustic cement tiles grow more lustrous and beautifully patinated the more they are walked upon over decades.",
            exampleLocationsVi = listOf("Chung cư 14 Tôn Thất Đạm (P. Bến Nghé)", "Cư Xá Đô Thành (P.4, Q.3)", "Hẻm Biệt Thự 18A Nguyễn Thị Minh Khai"),
            exampleLocationsEn = listOf("14 Ton That Dam Vintage Tenement", "Do Thanh Housing Estate (D3)", "Alley 18A Nguyen Thi Minh Khai")
        ),
        CulturalGlossaryItem(
            id = "bang_hieu_ve_tay",
            term = "Bảng Hiệu Chữ Vẽ Tay Thập Niên 70-80",
            phonetic = "[ɓaːŋ³¹³ hiw³² vɛʔ⁴⁵ taj³³]",
            toneGuide = "Thanh hỏi - thanh nặng - thanh ngã - thanh ngang",
            category = GlossaryCategory.ARCHITECTURE,
            icon = "🎨",
            accentColor = Color(0xFFEF4444),
            shortDefinitionVi = "Nghệ thuật kẻ chữ typography sơn dầu thủ công trên biển tôn của các tiệm tạp hóa, cắt tóc đầu hẻm.",
            shortDefinitionEn = "Hand-lettered oil enamel typography signboards hand-painted on corrugated tin outside alley barbershops and grocery stores.",
            shortDefinitionZh = "深巷杂货店、理发摊和修车档门前手工油漆绘制的怀旧字体铁皮招牌。",
            shortDefinitionJa = "路地の散髪屋や個人商店の店先に残る、ペンキで手描きされたレトロなタイポグラフィ看板。",
            shortDefinitionKo = "골목 이발소와 구멍가게 앞에 걸린 70~80년대 감성의 수제 페인트 레터링 양철 간판.",
            fullDescriptionVi = "Trước khi có in bạt kỹ thuật số, mọi bảng hiệu hẻm phố đều được vẽ tay bởi các họa sĩ đường phố. Những đường nét chữ đậm chất Art Deco pha lẫn thư pháp phương Đông với màu sơn đỏ tươi, vàng nghệ và xanh cổ vịt tạo nên bản sắc độc bản cho từng góc phố.",
            fullDescriptionEn = "Before digital printing, all Saigon alley signboards were hand-lettered by vernacular street sign painters. Blending modernist Art Deco strokes with local warmth, these weather-beaten signs represent the authentic visual culture of Saigon.",
            fullDescriptionZh = "在数码喷绘普及之前，西贡深巷的所有门头均由民间画工手绘完成。融合装饰艺术风格与市井风味，形成了历经风雨仍韵味十足的招牌艺术。",
            fullDescriptionJa = "デジタル印刷以前、路地の看板はすべて地元の職人が手描きしていました。アールデコ調と庶民的な温かみが溶け合う貴重な街頭美術です。",
            fullDescriptionKo = "디지털 인쇄 시대 이전에 거리 장인들이 직접 손으로 칠한 간판. 독특한 폰트와 원색의 조화가 사이공 골목의 레트로한 정취를 완성합니다.",
            whyItMattersVi = "Di sản nghệ thuật đường phố đang dần biến mất cần được trân trọng và lưu giữ qua các bức ảnh nhiệm vụ.",
            whyItMattersEn = "An endangered street art form that walkers can document and appreciate before it fades from modern streets.",
            whyItMattersZh = "弥足珍贵的濒危街头民间艺术，值得在探索过程中拍照记录与珍视。",
            whyItMattersJa = "失われつつある貴重な路上アート。写真チャレンジで記録し、その価値を再発見できます。",
            whyItMattersKo = "사라져가는 소중한 거리 예술로, 사진 챌린지를 통해 기록하고 보존해야 할 도시 유산입니다.",
            triviaVi = "Mỗi thợ vẽ bảng hiệu xưa đều có bí quyết pha dầu bóng và bột màu để chịu được mưa nắng nhiệt đới suốt hơn 30 năm.",
            triviaEn = "Master sign-painters used custom varnish blends allowing their outdoor tin signs to survive tropical monsoons for 30+ years.",
            exampleLocationsVi = listOf("Hẻm thợ sửa đồng hồ Đặng Dung (P. Tân Định, Q.1)", "Phố đông y Lương Nhữ Học (Q.5)", "Hẻm 493 Tô Hiến Thành (Q.10)"),
            exampleLocationsEn = listOf("Dang Dung Watchmakers Alley (D1)", "Luong Nhu Hoc Herbalist Row (D5)", "Alley 493 To Hien Thanh (D10)")
        ),
        CulturalGlossaryItem(
            id = "hem_lam_dan",
            term = "Hẻm Đàn Ghi-ta Thủ Công Nguyễn Thiện Thuật",
            phonetic = "[hɛm³³ laːm²¹ ɗaːn²¹]",
            toneGuide = "Thanh ngã - thanh huyền - thanh huyền",
            category = GlossaryCategory.COMMUNITY,
            icon = "🎸",
            accentColor = Color(0xFF8B5CF6),
            shortDefinitionVi = "Con hẻm huyền thoại quy tụ hàng chục xưởng đẽo gọt gỗ và chế tác đàn guitar, violin thủ công hơn nửa thế kỷ.",
            shortDefinitionEn = "The legendary luthier alleyway lined with multi-generational workshops handcrafting acoustic guitars and violins.",
            shortDefinitionZh = "汇聚了数十家历经半个多世纪手工木工制琴作坊的传奇吉他之巷。",
            shortDefinitionJa = "半世紀以上にわたりアコースティックギターやバイオリンを手作りし続ける弦楽器職人の路地。",
            shortDefinitionKo = "반세기 넘게 통기타와 바이올린을 수제작해 온 장인 공방들이 밀집한 전설적인 악기 골목.",
            fullDescriptionVi = "Bước vào hẻm Nguyễn Thiện Thuật, mùi thơm của gỗ vân sam, gỗ cẩm lai và tiếng gảy đàn thử dây ngân vang khắp không gian. Các nghệ nhân nơi đây vẫn kiên trì uốn hông đàn bằng nhiệt, gọt nan hoa và căn chỉnh âm sắc thủ công từng cây đàn.",
            fullDescriptionEn = "Entering Nguyen Thien Thuat alley, the fragrance of spruce and rosewood mingles with acoustic melodies floating from open doorways. Master luthiers here bend wooden ribs over heated pipes and hand-chisel sound braces with timeless devotion.",
            fullDescriptionZh = "踏入阮善述街深巷，空气中弥漫着云杉与紫檀木的清香，伴随着清脆的试弦音符。制琴师们依然坚守手工热弯琴体、雕琢音梁的精湛传统。",
            fullDescriptionJa = "路地に足を踏み入れると、木材の芳香と試し弾きの澄んだ音色が響き渡ります。伝統技法で一本ずつ楽器に命を吹き込む職人たちの情熱が息づいています。",
            fullDescriptionKo = "골목에 들어서면 은은한 원목 향과 함께 기타 줄을 튜닝하는 맑은 선율이 울려 퍼집니다. 전통 방식으로 악기를 다듬는 장인들의 숨결을 느낄 수 있습니다.",
            whyItMattersVi = "Làng nghề giữa lòng phố: Bảo tồn âm thanh acoustic mộc mạc của tâm hồn nghệ thuật phương Nam.",
            whyItMattersEn = "Living urban guild: Preserving the authentic acoustic craft and bohemian artistic soul of Saigon.",
            whyItMattersZh = "闹市中的手工艺村落：传承着西贡最纯粹朴实的声学手工造诣与音乐情怀。",
            whyItMattersJa = "都会に息づく職人街：サイゴンの温もりある手作り音楽文化を今に伝える貴重な場所です。",
            whyItMattersKo = "도심 속 장인 거리: 사이공의 서정적인 음악과 전통 수공예 정신을 묵묵히 이어가는 특별한 공간.",
            triviaVi = "Nhiều nghệ sĩ guitar nổi tiếng quốc tế từng tìm đến con hẻm này để đặt làm riêng những cây đàn gỗ cẩm lai Việt Nam độc bản.",
            triviaEn = "International acoustic musicians frequently visit this alley to commission custom guitars crafted from indigenous Vietnamese tonewoods.",
            exampleLocationsVi = listOf("Dãy phố đàn Nguyễn Thiện Thuật (P.2, Q.3)", "Hẻm 109 Nguyễn Thiện Thuật", "Hẻm Cư Xá Đô Thành"),
            exampleLocationsEn = listOf("Nguyen Thien Thuat Guitar Street (D3)", "Alley 109 Nguyen Thien Thuat", "Do Thanh Quarter")
        ),
        CulturalGlossaryItem(
            id = "chua_ong_ba",
            term = "Chùa Bà Thiên Hậu & Chùa Ông Chợ Lớn",
            phonetic = "[tɕuə²¹ ɓaː²¹ tʰiən³³ həw³²]",
            toneGuide = "Thanh huyền - thanh huyền - thanh ngang - thanh nặng",
            category = GlossaryCategory.HERITAGE,
            icon = "🛕",
            accentColor = Color(0xFFDC2626),
            shortDefinitionVi = "Những ngôi miếu cổ linh thiêng hàng trăm năm với nhang vòng khổng lồ treo trần và phù điêu gốm men Cây Mai.",
            shortDefinitionEn = "Centuries-old Cantonese clan temples renowned for giant ceiling incense coils and intricate Cay Mai ceramic roof friezes.",
            shortDefinitionZh = "拥有数百年历史的岭南风格古庙，以殿顶悬挂的巨型盘香与精美绝伦的“梅树窑”彩色陶塑屋脊闻名。",
            shortDefinitionJa = "巨大な渦巻き線香が天井から下がり、屋根にマイ樹窯の色鮮やかな陶器彫刻が輝く数百年の歴史ある華人古寺。",
            shortDefinitionKo = "천장에 매달린 대형 나선형 향과 지붕을 장식한 정교한 도자기 부조로 유명한 유서 깊은 사원.",
            fullDescriptionVi = "Xây dựng từ giữa thế kỷ 18 bởi các di dân Quảng Đông và Phúc Kiến, các ngôi miếu như Chùa Bà (Tuệ Thành Hội Quán) và Chùa Ông (Nghĩa An Hội Quán) là trung tâm tín ngưỡng và tương trợ cộng đồng. Không gian mờ ảo khói nhang và ánh sáng giếng trời tạo nên vẻ đẹp tâm linh lắng đọng.",
            fullDescriptionEn = "Erected in the mid-18th century by maritime merchants, these assembly hall temples served as community anchors. Sunlight streams through central courtyards, illuminating spiraling incense coils that burn for a whole month with written prayers.",
            fullDescriptionZh = "由18世纪中叶南渡的华商侨领集资兴建，既是信仰圣地亦是同乡互助枢纽。阳光穿透天井洒向袅袅盘香，庄严而宁静。",
            fullDescriptionJa = "18世紀半ばに建立された華人コミュニティの心の拠り所。天窓から差し込む光と渦巻き線香の煙が幻想的な静寂を生み出します。",
            fullDescriptionKo = "18세기 중반 화교 상인들이 세운 유서 깊은 사원으로, 천장에서 타오르는 나선형 향과 하늘이 열린 안마당의 채광이 경건함을 자아냅니다.",
            whyItMattersVi = "Di tích kiến trúc nghệ thuật đỉnh cao kết tinh văn hóa Chợ Lớn hàng trăm năm lịch sử.",
            whyItMattersEn = "A masterpiece of historical architecture embodying centuries of maritime heritage and community resilience.",
            whyItMattersZh = "堪称岭南古建艺术在南洋的集大成者，见证了堤岸华社波澜壮阔的百年历史。",
            whyItMattersJa = "南洋の中華建築美の極致であり、チョロンの歴史的アイデンティティを象徴する最高峰の文化遺産です。",
            whyItMattersKo = "오랜 세월을 간직한 건축 예술의 정수로, 사이공 초롱 지역의 다문화적 깊이를 상징합니다.",
            triviaVi = "Mỗi khoanh nhang vòng lớn có thể cháy liên tục suốt 30 ngày đêm, mang theo ước nguyện bình an của người gửi gắm.",
            triviaEn = "Each large hanging incense coil burns continuously for 30 full days, carrying written family blessings toward the heavens.",
            exampleLocationsVi = listOf("Chùa Bà Thiên Hậu (710 Nguyễn Trãi, P.11, Q.5)", "Nghĩa An Hội Quán (678 Nguyễn Trãi, Q.5)", "Hào Sĩ Phường (206 Trần Hưng Đạo)"),
            exampleLocationsEn = listOf("Thien Hau Pagoda (710 Nguyen Trai, D5)", "Nghia An Assembly Hall (678 Nguyen Trai, D5)", "Hao Si Phuong Courtyard")
        ),
        CulturalGlossaryItem(
            id = "cu_xa_do_thanh",
            term = "Cư Xá (Khu Nhà Ở Cộng Đồng Thập Niên 60-70)",
            phonetic = "[kɨ³³ saː⁴⁵]",
            toneGuide = "Thanh ngang - thanh sắc",
            category = GlossaryCategory.ARCHITECTURE,
            icon = "🏢",
            accentColor = Color(0xFF2563EB),
            shortDefinitionVi = "Mô hình khu quy hoạch cư xá nhà phố và chung cư tầng thấp thập niên 1960-1970 với nhiều giếng trời và ban công xanh.",
            shortDefinitionEn = "Mid-century modernist residential quarter concept designed in the 1960s-70s with shaded courtyards and breeze blocks.",
            shortDefinitionZh = "20世纪60-70年代规划的现代主义低层住宅与排屋社区，以通风天井和绿植阳台著称。",
            shortDefinitionJa = "1960〜70年代に設計された中層集合住宅地。風通しの良い中庭や花型ブロックが特徴的な昭和モダンに似た風情。",
            shortDefinitionKo = "1960~70년대에 조성된 중저층 주거 단지 모델로, 통풍이 잘되는 발코니와 정겨운 이웃 커뮤니티가 특징.",
            fullDescriptionVi = "Cư xá (như Cư Xá Đô Thành, Cư Xá Thanh Đa, Cư Xá Lữ Gia) là di sản kiến trúc nhiệt đới độc đáo của Sài Gòn. Nơi đây kết hợp hài hòa giữa lối sống đô thị hiện đại và tình làng nghĩa xóm, với quán cà phê cóc dưới bóng cây và các tiệm ăn gia truyền phục vụ cư dân.",
            fullDescriptionEn = "Cư Xá quarters represent Saigon's golden era of tropical modernism. Designed with shaded breeze corridors, porous facades, and central community plazas, they foster intimate neighborly bonds amidst dense urban growth.",
            fullDescriptionZh = "低层居住区是西贡热带现代主义建筑的典范。通透的花格砖、绿荫环抱的林荫巷道与楼下亲切的咖啡小店，构筑起温馨悠闲的社区生态。",
            fullDescriptionJa = "熱帯モダニズム建築の傑作群。日陰を作る回廊や植物に覆われたバルコニーが心地よい風を呼び込み、穏やかな暮らしを育んでいます。",
            fullDescriptionKo = "사이공의 독특한 열대 모더니즘 건축 양식. 그늘진 복도와 발코니 화단, 동네 마당이 어우러져 이웃 간의 따뜻한 유대감을 형성합니다.",
            whyItMattersVi = "Di sản sống: Cảm nhận nhịp thở yên bình, chậm rãi và tình người thuần hậu giữa lòng thành phố náo nhiệt.",
            whyItMattersEn = "Living heritage: Offers an unhurried glimpse into neighborly warmth and mid-century architectural elegance.",
            whyItMattersZh = "活态遗产：在繁华大都市中寻得一份难得的宁静步调与淳朴街坊情谊。",
            whyItMattersJa = "生きた遺産：大都市の中心にいながら、ゆったりとした時間の流れと人情味に出会える特別な場所です。",
            whyItMattersKo = "살아있는 유산: 번화한 도심 속에서 여유롭고 따뜻한 이웃들의 온기를 느낄 수 있는 공간입니다.",
            triviaVi = "Cư Xá Đô Thành vốn được xây dựng trên nền trường đua ngựa cũ của Sài Gòn từ trước năm 1954.",
            triviaEn = "Cu Xa Do Thanh was originally developed atop the historic grounds of the pre-1954 Saigon Hippodrome horse racetrack.",
            exampleLocationsVi = listOf("Cư Xá Đô Thành (P.4, Q.3)", "Cư Xá Lô S Thanh Đa (Bình Thạnh)", "Cư Xá Lữ Gia (P.15, Q.11)"),
            exampleLocationsEn = listOf("Do Thanh Quarter (District 3)", "Thanh Da Block S (Binh Thanh)", "Lu Gia Quarter (District 11)")
        ),
        CulturalGlossaryItem(
            id = "banh_trang_nuong",
            term = "Bánh Tráng Nướng Mâm Than & Bánh Tráng Trộn",
            phonetic = "[ɓajŋ³⁵ tɕaːŋ³⁵ nɨəŋ³⁵]",
            toneGuide = "Thanh sắc - thanh sắc - thanh sắc",
            category = GlossaryCategory.CULINARY,
            icon = "🍘",
            accentColor = Color(0xFFD97706),
            shortDefinitionVi = "'Pizza Sài Gòn' nướng giòn rụm trên than hồng đầu hẻm, món ăn vặt gắn liền với thanh xuân học trò.",
            shortDefinitionEn = "Crispy rice paper grilled over live red charcoal with quail eggs, scallion oil, and dried shrimp on sidewalk stools.",
            shortDefinitionZh = "炭火慢烤香脆米纸与爽口拌米纸，被誉为“西贡披萨”，是深巷中最具人气的青春街头小吃。",
            shortDefinitionJa = "炭火で香ばしく焼き上げる「ベトナム風ピザ」。ウズラの卵や干しエビが乗った路地裏の定番おやつ。",
            shortDefinitionKo = "숯불에 바삭하게 구워 메추리알과 건새우를 올린 '사이공 피자', 골목길 최고의 길거리 간식.",
            fullDescriptionVi = "Chỉ cần một lò than nhỏ, vỉ nướng tròn và vài chiếc ghế đẩu nhựa, gánh bánh tráng nướng có thể thắp sáng cả góc hẻm khi hoàng hôn buông xuống. Hương bơ thơm nức, trứng cút béo ngậy cùng tép khô giòn rụm tạo nên hương vị khó quên.",
            fullDescriptionEn = "With a portable brazier and a few plastic stools, rice paper grilled over coals turns dark alley corners into vibrant twilight gatherings. The buttery aroma, savory dried shrimp, and crackling crisp texture define Saigon street nostalgia.",
            fullDescriptionZh = "仅凭一只炭火小炉与几张小板凳，烤米纸摊便能在黄昏时分点亮整条深巷。黄油的焦香、鹌鹑蛋的浓郁与香脆虾米交织出难以忘怀的市井美味。",
            fullDescriptionJa = "小さな炭火コンロの周りに人が集まり、香ばしいバターとエビの香りが漂う夕暮れの路地裏名物です。",
            fullDescriptionKo = "작은 숯불 화로 주변에 모여 앉아 갓 구워낸 바삭한 라이스페이퍼를 즐기는 사이공의 정겨운 오후 풍경.",
            whyItMattersVi = "Ẩm thực đường phố bình dân thắt chặt tình bạn bè và lưu giữ ký ức tuổi trẻ của bao thế hệ.",
            whyItMattersEn = "Grassroots culinary culture that brings friends together and fuels youthful memories across generations.",
            whyItMattersZh = "凝聚街坊情谊与青春记忆的草根美食文化典范。",
            whyItMattersJa = "学生や若者たちが集い、語らいながら味わう下町ソウルフードの象徴です。",
            whyItMattersKo = "친구들과 도란도란 모여 앉아 추억을 나누는 사이공 청춘들의 소울푸드.",
            triviaVi = "Bánh tráng nướng gốc từ Đà Lạt khi về đến các hẻm Sài Gòn đã được biến tấu thêm phô mai, xúc xích và sốt mayonnaise béo thơm.",
            triviaEn = "Originally inspired by Da Lat, Saigon alley cooks added creamy laughing-cow cheese and rich mayo toppings.",
            exampleLocationsVi = listOf("Hẻm 493 Tô Hiến Thành (Cổng 3 BK)", "Hẻm 14 Tôn Thất Đạm (Q.1)", "Hẻm 206 Trần Hưng Đạo (Q.5)"),
            exampleLocationsEn = listOf("Alley 493 To Hien Thanh (HCMUT Gate 3)", "Alley 14 Ton That Dam (D1)", "Alley 206 Tran Hung Dao (D5)")
        ),
        CulturalGlossaryItem(
            id = "hem_sach_cu",
            term = "Hẻm Sách Cũ & Bản Thảo Xưa",
            phonetic = "[hɛm³³ sax³⁵ kuʔ⁴⁵]",
            toneGuide = "Thanh ngã - thanh sắc - thanh ngã",
            category = GlossaryCategory.COMMUNITY,
            icon = "📖",
            accentColor = Color(0xFF6366F1),
            shortDefinitionVi = "Những kệ sách nhuốm màu thời gian ẩn mình trong hẻm sâu, lưu giữ hàng vạn ấn phẩm văn hóa và tư liệu quý.",
            shortDefinitionEn = "Time-worn antique bookstores tucked away in quiet alleys, preserving rare vintage volumes and cultural manuscripts.",
            shortDefinitionZh = "隐匿于幽静深巷中的旧书肆，珍藏着数以万计泛黄的珍贵文史典籍与旧版译著。",
            shortDefinitionJa = "路地の奥深くに佇む古書店街。半世紀前の貴重な文学書や歴史資料が静かに眠る文化の宝庫。",
            shortDefinitionKo = "조용한 골목 안쪽에 자리한 헌책방들로, 수만 권의 고서와 문학 서적이 시간의 향기를 머금고 있는 곳.",
            fullDescriptionVi = "Khác với các hiệu sách mặt phố ồn ào, tiệm sách cũ trong hẻm là nơi độc giả có thể ngồi hàng giờ dưới quạt máy cũ, nhâm nhi tách trà nóng và tìm kiếm những tác phẩm xuất bản từ những năm 1960-1970.",
            fullDescriptionEn = "Far from noisy commercial bookstores, alley secondhand book dens invite seekers to lose track of time under spinning ceiling fans, sipping green tea while leafing through mid-century paperbacks and forgotten maps.",
            fullDescriptionZh = "远离主干道喧嚣，深巷旧书铺让爱书人能在老吊扇下静坐数小时，品一杯清茶，淘寻尘封半个世纪的泛黄书页。",
            fullDescriptionJa = "大通りの喧騒を忘れ、天井扇の風を感じながら緑茶片手に貴重な古書との一期一会を楽しめる静謐な空間です。",
            fullDescriptionKo = "도심의 소음에서 벗어나 옛 선풍기 바람 아래 따뜻한 차 한 잔과 함께 보물 같은 고서들을 찾아보는 아늑한 공간.",
            whyItMattersVi = "Lưu trữ ký ức học thuật và tình yêu tri thức bền bỉ của người Sài Gòn qua nhiều biến thiên thời cuộc.",
            whyItMattersEn = "Preserves the intellectual soul, literary heritage, and enduring love of reading in Saigon.",
            whyItMattersZh = "默默守护着西贡几代知识分子的精神家园与持久的读书情怀。",
            whyItMattersJa = "時代の移り変わりの中でも変わらない、サイゴンの知的好奇心と読書文化を守り続ける場所です。",
            whyItMattersKo = "세월의 변화 속에서도 변치 않는 사이공 사람들의 학문적 열정과 독서 문화를 간직한 보금자리.",
            triviaVi = "Nhiều chủ tiệm sách cũ là các nhà giáo, học giả về hưu có thể kể vanh vách lai lịch của từng bản thảo quý.",
            triviaEn = "Many alley booksellers are retired scholars who can recount the personal publishing history of every vintage title.",
            exampleLocationsVi = listOf("Hẻm sách cũ Trần Nhân Tôn (P.2, Q.10)", "Hẻm 158 Pasteur (Q.1)", "Hẻm 493 Tô Hiến Thành (Q.10)"),
            exampleLocationsEn = listOf("Tran Nhan Ton Vintage Book Row (D10)", "Alley 158 Pasteur (D1)", "Alley 493 To Hien Thanh (D10)")
        ),
        CulturalGlossaryItem(
            id = "vuon_xanh_ban_cong",
            term = "Vườn Cây Hẻm Phố & Giàn Hoa Giấy",
            phonetic = "[vɨən²¹ sajŋ³³ ɓaːn³³ kawŋm³³]",
            toneGuide = "Thanh huyền - thanh ngang - thanh ngang - thanh ngang",
            category = GlossaryCategory.COMMUNITY,
            icon = "🌺",
            accentColor = Color(0xFF10B981),
            shortDefinitionVi = "Ốc đảo xanh mát do cư dân tự tay trồng bằng chậu kiểng, giàn thiên lý và hoa giấy rực rỡ lọc mát hẻm nhỏ.",
            shortDefinitionEn = "Lush micro-gardens, hanging orchids, and vibrant bougainvillea trellises cultivated by alley residents to cool the laneways.",
            shortDefinitionZh = "居民亲手在门前与阳台栽种的绿植盆栽、藤萝与九重葛花架，构筑成深巷中的天然清凉绿洲。",
            shortDefinitionJa = "住民たちが丹精込めて育てる鉢植えやブーゲンビリアの花棚。熱帯の路地を涼しく潤す緑のオアシス。",
            shortDefinitionKo = "골목 주민들이 직접 가꾼 화분과 부겐빌레아 꽃 덩굴이 만들어내는 도심 속 싱그러운 녹색 오아시스.",
            fullDescriptionVi = "Không có sân vườn rộng, người Sài Gòn biến từng bậc thềm, bờ tường và ban công hẻm thành một khu vườn nhiệt đới thu nhỏ. Những chùm hoa giấy rực rỡ dưới nắng cùng chậu trầu bà, cây phát tài mang lại bầu không khí trong lành và cảm giác thân thương.",
            fullDescriptionEn = "Adapting to tight living spaces, residents transform every doorstep, wall ledge, and overhead balcony into vertical gardens. Cascading magenta bougainvillea and potted ferns bring natural shade, cooling breeze, and poetic charm to compact alley corridors.",
            fullDescriptionZh = "即便没有宽阔庭院，西贡人也能将台阶、窗沿与阳台装点成微型热带花园。艳丽的三角梅与翠绿盆栽为狭窄深巷带来无尽生机与清凉。",
            fullDescriptionJa = "限られた空間を活かし、玄関先や手すりを緑で彩る住民の知恵。鮮やかな花々が路地を涼しく包み込みます。",
            fullDescriptionKo = "작은 공간을 지혜롭게 활용하여 문 앞과 발코니를 미니 열대 정원으로 꾸민 주민들의 따뜻한 삶의 여유.",
            whyItMattersVi = "Giải pháp xanh tự nhiên: Giảm nhiệt đô thị và lan tỏa lối sống bền vững, hài hòa với thiên nhiên.",
            whyItMattersEn = "Community-led green cooling that reduces urban heat and radiates sustainable, nature-loving harmony.",
            whyItMattersZh = "民间自发的绿色降温智慧，体现了人与自然和谐共生的可持续生态理念。",
            whyItMattersJa = "都市の熱を和らげ、自然と共に暮らすサステナブルな下町の知恵が詰まっています。",
            whyItMattersKo = "도심의 열기를 식혀주고 자연과 조화롭게 살아가는 지속 가능한 골목 생태계의 모범.",
            triviaVi = "Nhiều con hẻm tại Sài Gòn đã được trao giải 'Tuyến hẻm Xanh - Sạch - Đẹp' nhờ phong trào phủ xanh cộng đồng.",
            triviaEn = "Entire neighborhoods regularly win civic awards for collective community greening and alleyway floral preservation.",
            exampleLocationsVi = listOf("Hẻm Biệt Thự 18A Nguyễn Thị Minh Khai (Q.1)", "Hẻm 493 Tô Hiến Thành (Q.10)", "Cư Xá Thanh Đa Lô IV (Bình Thạnh)"),
            exampleLocationsEn = listOf("Alley 18A Nguyen Thi Minh Khai (D1)", "Alley 493 To Hien Thanh (D10)", "Thanh Da Block IV (Binh Thanh)")
        )
    )

    private val _glossaryItems = MutableStateFlow<List<CulturalGlossaryItem>>(DEFAULT_ITEMS)
    val glossaryItemsState: StateFlow<List<CulturalGlossaryItem>> = _glossaryItems.asStateFlow()

    val items: List<CulturalGlossaryItem>
        get() = _glossaryItems.value

    /**
     * Seeds the initial cultural glossary into Firebase Firestore 'cultural_glossary' collection if empty.
     */
    suspend fun seedFirestoreIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext false
        try {
            val snapshot = fs.collection(COLLECTION_NAME).limit(1).get().await()
            if (!snapshot.isEmpty) {
                Log.d(TAG, "Firestore '$COLLECTION_NAME' already contains entries. Skipping seeder.")
                return@withContext false
            }

            Log.i(TAG, "Seeding ${DEFAULT_ITEMS.size} cultural terms into Firestore '$COLLECTION_NAME'...")
            for (item in DEFAULT_ITEMS) {
                val map = itemToMap(item)
                fs.collection(COLLECTION_NAME).document(item.id).set(map, SetOptions.merge()).await()
            }
            Log.i(TAG, "Successfully seeded cultural glossary items to Firestore.")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Firestore glossary seeding notice: ${e.message}")
            false
        }
    }

    /**
     * Pulls and merges cultural glossary terms from Firebase Firestore 'cultural_glossary'.
     */
    suspend fun syncFromFirestore(): List<CulturalGlossaryItem> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext items
        try {
            // Check & seed if empty first
            seedFirestoreIfNeeded()

            val snapshot = fs.collection(COLLECTION_NAME).get().await()
            if (snapshot.isEmpty) {
                return@withContext items
            }

            val cloudMap = mutableMapOf<String, CulturalGlossaryItem>()
            for (doc in snapshot.documents) {
                val item = mapToItem(doc.id, doc.data ?: emptyMap())
                if (item != null) {
                    cloudMap[item.id] = item
                }
            }

            // Merge cloud items with default items
            val mergedMap = LinkedHashMap<String, CulturalGlossaryItem>()
            for (def in DEFAULT_ITEMS) {
                mergedMap[def.id] = def
            }
            for ((k, v) in cloudMap) {
                mergedMap[k] = v
            }

            val resultList = mergedMap.values.toList()
            _glossaryItems.value = resultList
            Log.d(TAG, "Synced ${resultList.size} cultural glossary items from Firestore.")
            resultList
        } catch (e: Exception) {
            Log.w(TAG, "Unable to pull glossary from Firestore: ${e.message}")
            items
        }
    }

    /**
     * Saves or updates a cultural glossary term directly to Firestore and local state.
     */
    suspend fun saveTermToFirestore(item: CulturalGlossaryItem): Boolean = withContext(Dispatchers.IO) {
        val currentList = _glossaryItems.value.toMutableList()
        val existingIdx = currentList.indexOfFirst { it.id == item.id }
        if (existingIdx >= 0) {
            currentList[existingIdx] = item
        } else {
            currentList.add(0, item)
        }
        _glossaryItems.value = currentList

        val fs = firestore ?: return@withContext true
        try {
            val map = itemToMap(item)
            fs.collection(COLLECTION_NAME).document(item.id).set(map, SetOptions.merge()).await()
            Log.d(TAG, "Saved term '${item.term}' [ID: ${item.id}] to Firestore '$COLLECTION_NAME'")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save term to Firestore: ${e.message}")
            false
        }
    }

    private fun itemToMap(item: CulturalGlossaryItem): Map<String, Any> {
        val colorHex = String.format("#%06X", (0xFFFFFF and item.accentColor.value.toLong().toInt()))
        return hashMapOf(
            "id" to item.id,
            "term" to item.term,
            "phonetic" to item.phonetic,
            "toneGuide" to item.toneGuide,
            "category" to item.category.name,
            "icon" to item.icon,
            "accentColorHex" to colorHex,
            "shortDefinitionVi" to item.shortDefinitionVi,
            "shortDefinitionEn" to item.shortDefinitionEn,
            "shortDefinitionZh" to item.shortDefinitionZh,
            "shortDefinitionJa" to item.shortDefinitionJa,
            "shortDefinitionKo" to item.shortDefinitionKo,
            "fullDescriptionVi" to item.fullDescriptionVi,
            "fullDescriptionEn" to item.fullDescriptionEn,
            "fullDescriptionZh" to item.fullDescriptionZh,
            "fullDescriptionJa" to item.fullDescriptionJa,
            "fullDescriptionKo" to item.fullDescriptionKo,
            "whyItMattersVi" to item.whyItMattersVi,
            "whyItMattersEn" to item.whyItMattersEn,
            "whyItMattersZh" to item.whyItMattersZh,
            "whyItMattersJa" to item.whyItMattersJa,
            "whyItMattersKo" to item.whyItMattersKo,
            "triviaVi" to item.triviaVi,
            "triviaEn" to item.triviaEn,
            "exampleLocationsVi" to item.exampleLocationsVi,
            "exampleLocationsEn" to item.exampleLocationsEn,
            "updatedAt" to System.currentTimeMillis()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToItem(docId: String, data: Map<String, Any>): CulturalGlossaryItem? {
        return try {
            val term = data["term"] as? String ?: return null
            val categoryStr = data["category"] as? String ?: GlossaryCategory.ALL.name
            val cat = try { GlossaryCategory.valueOf(categoryStr) } catch (e: Exception) { GlossaryCategory.ALL }
            val colorHex = data["accentColorHex"] as? String ?: "#00B14F"
            val color = try {
                val cleanHex = colorHex.removePrefix("#")
                val parsedLong = cleanHex.toLong(16)
                if (cleanHex.length <= 6) Color(0xFF000000 or parsedLong)
                else Color(parsedLong)
            } catch (e: Exception) {
                Color(0xFF00B14F)
            }

            CulturalGlossaryItem(
                id = (data["id"] as? String) ?: docId,
                term = term,
                phonetic = (data["phonetic"] as? String) ?: "",
                toneGuide = (data["toneGuide"] as? String) ?: "",
                category = cat,
                icon = (data["icon"] as? String) ?: "📚",
                accentColor = color,
                shortDefinitionVi = (data["shortDefinitionVi"] as? String) ?: "",
                shortDefinitionEn = (data["shortDefinitionEn"] as? String) ?: "",
                shortDefinitionZh = (data["shortDefinitionZh"] as? String) ?: "",
                shortDefinitionJa = (data["shortDefinitionJa"] as? String) ?: "",
                shortDefinitionKo = (data["shortDefinitionKo"] as? String) ?: "",
                fullDescriptionVi = (data["fullDescriptionVi"] as? String) ?: "",
                fullDescriptionEn = (data["fullDescriptionEn"] as? String) ?: "",
                fullDescriptionZh = (data["fullDescriptionZh"] as? String) ?: "",
                fullDescriptionJa = (data["fullDescriptionJa"] as? String) ?: "",
                fullDescriptionKo = (data["fullDescriptionKo"] as? String) ?: "",
                whyItMattersVi = (data["whyItMattersVi"] as? String) ?: "",
                whyItMattersEn = (data["whyItMattersEn"] as? String) ?: "",
                whyItMattersZh = (data["whyItMattersZh"] as? String) ?: "",
                whyItMattersJa = (data["whyItMattersJa"] as? String) ?: "",
                whyItMattersKo = (data["whyItMattersKo"] as? String) ?: "",
                triviaVi = (data["triviaVi"] as? String) ?: "",
                triviaEn = (data["triviaEn"] as? String) ?: "",
                exampleLocationsVi = (data["exampleLocationsVi"] as? List<String>) ?: emptyList(),
                exampleLocationsEn = (data["exampleLocationsEn"] as? List<String>) ?: emptyList()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing glossary doc $docId: ${e.message}")
            null
        }
    }

    fun search(query: String, category: GlossaryCategory = GlossaryCategory.ALL, language: String = "vi"): List<CulturalGlossaryItem> {
        return items.filter { item ->
            val matchesCategory = category == GlossaryCategory.ALL || item.category == category
            val q = query.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                    item.term.lowercase().contains(q) ||
                    item.shortDefinitionVi.lowercase().contains(q) ||
                    item.shortDefinitionEn.lowercase().contains(q) ||
                    item.shortDefinitionZh.lowercase().contains(q) ||
                    item.shortDefinitionJa.lowercase().contains(q) ||
                    item.shortDefinitionKo.lowercase().contains(q) ||
                    item.fullDescriptionVi.lowercase().contains(q) ||
                    item.fullDescriptionEn.lowercase().contains(q) ||
                    item.whyItMattersVi.lowercase().contains(q) ||
                    item.whyItMattersEn.lowercase().contains(q) ||
                    item.exampleLocationsVi.any { it.lowercase().contains(q) } ||
                    item.exampleLocationsEn.any { it.lowercase().contains(q) }

            matchesCategory && matchesQuery
        }
    }

    fun findById(id: String): CulturalGlossaryItem? {
        return items.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }

    fun findMatchingTermsForText(text: String): List<CulturalGlossaryItem> {
        val lowerText = text.lowercase()
        return items.filter { item ->
            lowerText.contains(item.term.lowercase()) ||
            (item.id == "hem" && (lowerText.contains("hẻm") || lowerText.contains("alley"))) ||
            (item.id == "xet" && (lowerText.contains("xẹt") || lowerText.contains("/")) ) ||
            (item.id == "bac_xiu" && (lowerText.contains("bạc xỉu") || lowerText.contains("sữa nóng"))) ||
            (item.id == "gach_bong_xua" && (lowerText.contains("gạch bông") || lowerText.contains("gạch hoa") || lowerText.contains("tile"))) ||
            (item.id == "bang_hieu_ve_tay" && (lowerText.contains("bảng hiệu") || lowerText.contains("vẽ tay") || lowerText.contains("signboard"))) ||
            (item.id == "hem_lam_dan" && (lowerText.contains("đàn") || lowerText.contains("guitar") || lowerText.contains("nguyễn thiện thuật"))) ||
            (item.id == "chua_ong_ba" && (lowerText.contains("thiên hậu") || lowerText.contains("chùa bà") || lowerText.contains("chùa ông") || lowerText.contains("nghĩa an"))) ||
            (item.id == "cu_xa_do_thanh" && (lowerText.contains("cư xá") || lowerText.contains("đô thành") || lowerText.contains("thanh đa"))) ||
            (item.id == "banh_trang_nuong" && (lowerText.contains("bánh tráng") || lowerText.contains("nướng than"))) ||
            (item.id == "hem_sach_cu" && (lowerText.contains("sách cũ") || lowerText.contains("bản thảo"))) ||
            (item.id == "vuon_xanh_ban_cong" && (lowerText.contains("hoa giấy") || lowerText.contains("vườn cây") || lowerText.contains("ban công"))) ||
            (item.id == "ca_phe_vot" && (lowerText.contains("vợt") || lowerText.contains("net coffee"))) ||
            (item.id == "ca_phe_coc" && (lowerText.contains("cà phê cóc") || lowerText.contains("ghế nhựa"))) ||
            (item.id == "biet_dong" && (lowerText.contains("biệt động") || lowerText.contains("bunker"))) ||
            (item.id == "cho_lon" && (lowerText.contains("chợ lớn") || lowerText.contains("chinatown"))) ||
            (item.id == "hao_si_phuong" && lowerText.contains("hào sĩ phường")) ||
            (item.id == "chung_cu_co" && (lowerText.contains("chung cư") || lowerText.contains("apartment") || lowerText.contains("tôn thất đạm"))) ||
            (item.id == "hu_tieu_go" && (lowerText.contains("hủ tiếu") || lowerText.contains("hủ tiếu gõ"))) ||
            (item.id == "tieng_rao" && (lowerText.contains("tiếng rao") || lowerText.contains("ve chai"))) ||
            (item.id == "ban_tho_than_tai" && (lowerText.contains("thần tài") || lowerText.contains("thổ địa"))) ||
            (item.id == "lang_nghe" && (lowerText.contains("làng nghề") || lowerText.contains("lồng đèn") || lowerText.contains("lantern"))) ||
            (item.id == "sui_cao" && (lowerText.contains("sủi cảo") || lowerText.contains("dumpling") || lowerText.contains("hà tôn quyền"))) ||
            (item.id == "com_tam" && (lowerText.contains("cơm tấm") || lowerText.contains("broken rice"))) ||
            (item.id == "tra_da_via_he" && (lowerText.contains("trà đá") || lowerText.contains("miễn phí"))) ||
            (item.id.contains("bk") && (lowerText.contains("bách khoa") || lowerText.contains("hcmut") || lowerText.contains("diên hồng") || lowerText.contains("đồ án") || lowerText.contains("lữ gia") || lowerText.contains("tô hiến thành")))
        }
    }
}
