package com.example.data

object DefaultSyllabusData {

    private fun gen(n: Int): List<String> = (1..n).map { "Lec $it" }

    val defaultData: Map<String, Map<String, List<String>>> = linkedMapOf(
        "Maths- (Arithmetic)" to linkedMapOf(
            "Ratio & Proportion" to gen(17),
            "Percentage" to gen(18),
            "Mixture & Alligation" to gen(9),
            "Profit & Loss" to gen(13),
            "Simple Interest" to gen(7),
            "Compound Interest" to gen(11),
            "Chain Rule" to gen(4),
            "Time & Work" to gen(8),
            "Pipe & Cistern" to gen(5),
            "Average" to gen(9),
            "Age" to gen(7),
            "Partnership" to gen(5),
            "Time Speed & Distance" to gen(10),
            "Train" to gen(3),
            "Boat and Stream" to gen(4),
            "Calculation" to gen(10),
            "Digital Sum" to gen(3)
        ),
        "Maths- (Advance)" to linkedMapOf(
            "Geometry (ज्यामिति)" to gen(27),
            "Algebra" to gen(21),
            "Algebra Recorded" to gen(13),
            "Trigonometry" to gen(19),
            "Trigonometry Recorded" to gen(12),
            "Height & Distance" to gen(5),
            "Permutation, Combination & Probability" to gen(8),
            "Quadratic Equation" to gen(5),
            "Coordination Geometry" to gen(8),
            "Polynomial" to gen(1),
            "Number System" to gen(21),
            "Statistics" to gen(5),
            "Simplification" to gen(7),
            "LCM & HCF" to gen(8),
            "Surds & Indices" to gen(10),
            "Data Interpretation" to gen(11),
            "Mensuration" to gen(29)
        ),
        "Maths" to linkedMapOf(
            "Ratio & Proportion" to gen(16),
            "Percentage" to gen(14),
            "Mixture & Alligation" to gen(6),
            "Profit & Loss" to gen(9),
            "Simple Interest" to gen(6),
            "Compound Interest" to gen(10),
            "Time & Work" to gen(10),
            "Pipe & Cistern" to gen(6),
            "Average" to gen(5),
            "Race" to gen(2),
            "Age" to gen(6),
            "Partnership" to gen(2),
            "Train" to gen(4),
            "Boat and Stream" to gen(4),
            "Time Speed & Distance" to gen(7),
            "Number System" to gen(11),
            "LCM & HCF" to gen(4),
            "Surds & Indices" to gen(3),
            "Algebra" to gen(5),
            "Trigonometry" to gen(7),
            "Geometry (ज्यामिति)" to gen(11),
            "Mensuration" to gen(12),
            "Coordination Geometry" to gen(4),
            "Statistics" to gen(6),
            "Permutation, Combination" to gen(2),
            "Probability" to gen(2),
            "Data Interpretation" to gen(5)
        ),
        "Reasoning" to linkedMapOf(
            "Venn Diagram" to gen(2),
            "Dice" to gen(3),
            "Mirror & Water Image" to gen(1),
            "Mathematical Operations" to gen(2),
            "Alphabet" to gen(4),
            "Letter Series" to gen(1),
            "Coding Decoding" to gen(4),
            "Analogy" to gen(2),
            "Classification" to gen(1),
            "Number Series" to gen(2),
            "Seating Arrangement" to gen(3),
            "Puzzle" to gen(1),
            "Inequality" to gen(2),
            "Non-verbal Analogy & Classification" to gen(1),
            "Non Verbal Series" to gen(1),
            "Syllogism" to gen(3),
            "Blood Relation" to gen(3),
            "Calendar" to gen(4),
            "Clock" to gen(2),
            "Distance & Direction" to gen(2),
            "Counting Of Figure" to gen(2),
            "Missing Number" to gen(1),
            "Ranking & Ordering" to gen(2),
            "Statement & Argument" to gen(1),
            "Paper Cutting, Figure Formation & Passage" to gen(1),
            "Cube & Cuboid, Cause & Effect" to gen(1),
            "Completion Of Figures" to gen(1)
        ),
        "Physics" to linkedMapOf(
            "दाब" to gen(3),
            "प्रकाश" to gen(14),
            "गति" to gen(5),
            "न्यूटन के गति के नियम" to gen(6),
            "कार्य, ऊर्जा और शक्ति" to gen(6),
            "गुरुत्वाकर्षण" to gen(5),
            "ध्वनि और तरंग" to gen(7),
            "ऊष्मा" to gen(9),
            "विद्युत" to gen(4),
            "चुम्बकत्व" to gen(2),
            "Unit & Dimension" to gen(2),
            "पदार्थ" to gen(3),
            "घर्षण" to gen(1),
            "SHM" to gen(1)
        ),
        "Chemistry" to linkedMapOf(
            "पदार्थ" to gen(2),
            "परमाणु" to gen(10),
            "अम्ल और क्षार" to gen(5),
            "PH Scale" to gen(4),
            "आवर्त सारणी" to gen(9),
            "रासायनिक बंध/आबंध" to gen(9),
            "Periodic Table (MCQ)" to gen(1),
            "गैसीय अवस्था" to gen(2),
            "विलयन और कोलाइड" to gen(5),
            "ईंधन" to gen(2),
            "हाइड्रोजन" to gen(2),
            "धातु व अधातु" to gen(5),
            "Organic Compound" to gen(3),
            "विद्युत-रसायन" to gen(2),
            "दैनिक जीवन में रसायन" to gen(4),
            "Important Facts" to gen(2)
        ),
        "Biology" to linkedMapOf(
            "कोशिका" to gen(9),
            "पाचन तंत्र" to gen(8),
            "उत्सर्जन तंत्र" to gen(3),
            "रक्त" to gen(5),
            "परिसंचरण तंत्र" to gen(6),
            "श्र्वसन तंत्र" to gen(3),
            "कंकाल तंत्र" to gen(5),
            "तंत्रिका तंत्र" to gen(5),
            "अंतस्रावी तंत्र" to gen(5),
            "प्रजनन तंत्र" to gen(4),
            "मानव स्वास्थ्य और रोग" to gen(8),
            "ऊतक" to gen(4),
            "Plant Morphology & Phychology" to gen(5),
            "मानव नेत्र" to gen(1),
            "Classification" to gen(10)
        ),
        "Static GK" to linkedMapOf(
            "कला से संबंधित महत्वपूर्ण तथ्य" to gen(1),
            "भारत के संगठन" to gen(1),
            "भारत में राष्ट्रीय उद्यान" to gen(1),
            "शास्त्रीय नृत्य" to gen(2),
            "महत्वपूर्ण दिवस एवं थीम" to gen(2),
            "भारत और विश्व में प्रथम" to gen(1),
            "खेल से संबंधित महत्वपूर्ण तथ्य" to gen(4),
            "कला एवं संस्कृति" to gen(1),
            "भारत की लोक भाषा" to gen(1),
            "GI Tag" to gen(1),
            "भारत में प्रथम पुरुष एवं महिला" to gen(1),
            "भारत की नदी घाटी परियोजनाएं" to gen(1),
            "भारत के प्रमुख त्यौहार एवं उत्सव" to gen(1),
            "ओलंपिक खेल" to gen(2),
            "देश राजधानी और मुद्रा" to gen(1),
            "अनुसंधान संथान" to gen(2),
            "भारत के राष्ट्रीय उद्यान एवं अभयारण्य" to gen(4),
            "Paralympic 2024" to gen(1),
            "भारत की पंचवर्षीय योजनाएँ" to gen(2),
            "भारत रत्न पुरस्कार" to gen(1),
            "अंतर्राष्ट्रीय संगठन" to gen(2),
            "विश्व विरासत स्थल" to gen(1),
            "राष्ट्रीय संगठन" to gen(1),
            "भारत के प्रमुख संगीत घराने" to gen(1),
            "प्रमुख संग्रहालय एवं समाधी स्थल" to gen(1),
            "भारत के प्रमुख दर्रे" to gen(1),
            "भारत एवं विश्व की प्रमुख झीले, जलप्रपात एवं नहरे" to gen(2),
            "भारत एवं विश्व की प्रमुख नदिया" to gen(1),
            "महत्वपूर्ण अनुच्छेद" to gen(1)
        ),
        "Physical Geography" to linkedMapOf(
            "Universe (ब्रह्माण्ड)" to gen(2),
            "तारों का जीवन चक्र" to gen(1),
            "सौरमंडल" to gen(2),
            "क्षुद्रग्रह, धुमकेतु, उपग्रह" to gen(3),
            "ज्वार-भाटा" to gen(1),
            "पृथ्वी" to gen(1),
            "वायुमंडल" to gen(2),
            "पर्वत, पठार एवं मैदान" to gen(3),
            "ज्वालामुखी" to gen(1),
            "ज्वालामुखी और भूकंप" to gen(2),
            "ऋतु परिवर्तन" to gen(1),
            "दाब और पवनें" to gen(3)
        ),
        "World Geography" to linkedMapOf(
            "दक्षिण अमेरिका" to gen(2),
            "उत्तरी अमेरिका" to gen(3),
            "यूरोप महाद्वीप" to gen(2),
            "अफ्रीका महाद्वीप" to gen(2),
            "ऑस्ट्रेलिया महाद्वीप" to gen(2),
            "एशिया महाद्वीप" to gen(1)
        ),
        "Indian Geography" to linkedMapOf(
            "भारत का सामान्य परिचय" to gen(6),
            "Island (द्वीप)" to gen(2),
            "पठार और पर्वत" to gen(7),
            "अपवाह तंत्र" to gen(9),
            "झीलें" to gen(1),
            "Forest (वन)" to gen(6),
            "परिवहन" to gen(5),
            "खनिज संसाधन" to gen(3),
            "Soil (मिट्टी)" to gen(3),
            "कृषि" to gen(4),
            "जनजाति" to gen(2)
        ),
        "Polity" to linkedMapOf(
            "Doubt And Talk Session" to gen(1),
            "Practice Session" to gen(3),
            "Test" to gen(3),
            "संविधान सभा" to gen(5),
            "भारतीय संविधान के स्रोत" to gen(1),
            "अनुसूचियाँ" to gen(2),
            "भाग-1" to gen(2),
            "भाग-2" to gen(3),
            "भाग-3" to gen(7),
            "भाग-4" to gen(2),
            "मौलिक कर्तव्य" to gen(1),
            "भारत के राष्ट्रपति" to gen(5),
            "उपराष्ट्रपति" to gen(1),
            "प्रधानमंत्री" to gen(1),
            "संसद" to gen(5),
            "सुप्रीम कोर्ट" to gen(1),
            "Comptroller and Auditor General of India" to gen(1),
            "राज्यपाल" to gen(1),
            "विधानमण्डल" to gen(1),
            "उच्च न्यायालय" to gen(2),
            "मुख्यमंत्री" to gen(1),
            "भाग-8,9" to gen(1),
            "भाग-9,10" to gen(1),
            "भाग-11, भाग-12, भाग-13" to gen(1),
            "भाग-14,15,16" to gen(1),
            "भाग-16,17,18,19" to gen(1),
            "भाग-20,21,22" to gen(1),
            "Commission" to gen(1),
            "प्रस्तावना" to gen(1),
            "भारत का संवैधानिक विकास" to gen(4)
        ),
        "Economics" to linkedMapOf(
            "Doubt Session" to gen(1),
            "Test" to gen(4),
            "Structure Of RBI" to gen(4),
            "Monetary Policy(मौद्रिक नीति)" to gen(3),
            "Cheque" to gen(3),
            "Money Market" to gen(4),
            "Capital Market" to gen(2),
            "Stock Market" to gen(1),
            "योजनाएं (Scheme)" to gen(3),
            "Budget (बजट)" to gen(4),
            "Census" to gen(3),
            "Five Year Plans(पंचवर्षीय योजनाएं)" to gen(4),
            "International Organization" to gen(3),
            "Inflation And Deflation" to gen(1),
            "Basel Norms" to gen(2),
            "National Income (राष्ट्रीय आय)" to gen(1)
        ),
        "History" to linkedMapOf(
            "Test" to gen(1),
            "सिंधु घाटी सभ्यता" to gen(3),
            "वैदिक सभ्यता" to gen(3),
            "जैन धर्म" to gen(2),
            "बौद्ध धर्म" to gen(2),
            "मगध साम्राज्य" to gen(2),
            "मौर्य साम्राज्य" to gen(3),
            "मौर्योत्तर काल" to gen(1),
            "गुप्त वंश" to gen(3),
            "इस्लाम धर्म" to gen(1),
            "अरब आक्रमण" to gen(1),
            "तुर्की आक्रमण" to gen(4),
            "गुलाम वंश" to gen(5),
            "खिलजी वंश" to gen(6),
            "तुगलक वंश (Tughlaq Dynasty)" to gen(1),
            "सैय्यद और लोदी वंश" to gen(2),
            "मुगल साम्राज्य" to gen(15),
            "मराठा साम्राज्य" to gen(2),
            "यूरोपीय कंपनियों का आगमन" to gen(4),
            "प्लासी और बक्सर का युद्ध" to gen(3),
            "सामाजिक सुधारक" to gen(4),
            "1857 की क्रांति" to gen(3),
            "कांग्रेस की स्थापना" to gen(2),
            "बंगाल विभाजन" to gen(2),
            "क्रांतिकारी आंदोलन" to gen(3),
            "गांधी युग" to gen(3),
            "भारत छोड़ो आंदोलन" to gen(2),
            "Test(Class)" to gen(3)
        ),
        "Computer" to linkedMapOf(
            "Introduction And Syllabus" to gen(1),
            "Basic Of Computer" to gen(2),
            "Internet" to gen(7),
            "Operating System" to gen(3),
            "MS Word" to gen(7),
            "Software" to gen(2),
            "History Of Computer" to gen(4),
            "Input Device" to gen(3),
            "Types Of Computer" to gen(2),
            "MS Excel" to gen(3),
            "Networking" to gen(5),
            "MS Power Point" to gen(2),
            "Cyber Security" to gen(4),
            "Data Representation" to gen(2),
            "Generation Of Computer" to gen(1),
            "Output Device" to gen(2),
            "Memory" to gen(3),
            "Programming Language" to gen(2),
            "Computer Architecture" to gen(2),
            "Mobile Communication" to gen(1),
            "Database Management System" to gen(3)
        ),
        "Environment" to linkedMapOf(
            "Introduction And Syllabus" to gen(1),
            "Extra Notes" to gen(4),
            "MCQ" to gen(4),
            "पारिस्थितिकी तंत्र" to gen(2),
            "पारिस्थितिक पिरामिड तथा पोषण स्तर" to gen(1),
            "जैव-भू-रासायनिक चक्र (Biogeochemical Cycle)" to gen(2),
            "प्रदूषण" to gen(5),
            "जैव विविधता (Biodiversity)" to gen(3),
            "Climate Change ( जलवायु परिवर्तन)" to gen(1)
        ),
        "Current Affairs" to linkedMapOf(
            "Current Affairs" to gen(71)
        ),
        "Special Class" to linkedMapOf(
            "Mixture And Alligation" to gen(1)
        ),
        "Talk Session" to linkedMapOf(
            "Talk Session" to gen(1),
            "Special Talk Session By Aditya Sir" to gen(6)
        ),
        "SMP" to linkedMapOf(
            "Special Class" to gen(6),
            "Result PDF" to gen(3)
        ),
        "ebook" to linkedMapOf(
            "Current Affairs eMagazine" to gen(1),
            "Static GK Sanjivani Book" to gen(1)
        )
    )

    val defaultNoteLabels: List<NoteLabel> = listOf(
        NoteLabel(id = "lbl_formula", name = "सूत्र", colorHex = "#4CC2FF", sortOrder = 0),
        NoteLabel(id = "lbl_trick", name = "ट्रिक", colorHex = "#34D399", sortOrder = 1),
        NoteLabel(id = "lbl_mistake", name = "गलती", colorHex = "#FB7185", sortOrder = 2),
        NoteLabel(id = "lbl_doubt", name = "संदेह", colorHex = "#FBBF24", sortOrder = 3),
        NoteLabel(id = "lbl_general", name = "सामान्य", colorHex = "#A1A1AA", sortOrder = 4)
    )

    val labelColorPalette: List<String> = listOf(
        "#4CC2FF", "#34D399", "#FB7185", "#FBBF24",
        "#A78BFA", "#FB923C", "#2DD4BF", "#A1A1AA"
    )

    data class StatusGuideItem(
        val key: String,
        val name: String,
        val subtitle: String,
        val whatItDoes: String,
        val difference: String? = null,
        val example: String,
        val whyNeeded: String,
        val withoutIt: String
    )

    const val GUIDE_INTRO =
        "सड़क और ड्राइवर का फर्क: सड़क कैसी है (सीधी, पहाड़ी या जटिल चौराहा) यह Notes के 5 स्तर बताते हैं, और उस सड़क पर आपकी ड्राइविंग कितनी पक्की है यह Session Status के 3 बटन बताते हैं। इससे आपकी खुद की तैयारी (Next Action) और सवाल का असली स्तर (Content Level) आपस में उलझते नहीं हैं।"

    const val STATUS_GUIDE_NOTE =
        "ये तीनों शब्द यह नहीं बताते कि टॉपिक कठिन है या आसान — ये बताते हैं कि इस लेक्चर के साथ अब आगे क्या करना है।"

    const val NOTE_LEVEL_GUIDE_NOTE =
        "इन्हें जिम के वज़न की तरह समझें — ये सामने रखे सवाल या कॉन्सेप्ट का असली वज़न बताते हैं।"

    val sessionStatusGuides: List<StatusGuideItem> = listOf(
        StatusGuideItem(
            key = "Easy",
            name = "Clear",
            subtitle = "पक्का / दिमाग में बैठ गया",
            whatItDoes = "यह उन लेक्चर्स को \"सुरक्षित ज़ोन\" में डालता है जिनका लॉजिक आपको पूरी तरह समझ आ चुका है और अब उन्हें तुरंत दोहराने की ज़रूरत नहीं है।",
            example = "जैसे साइकिल का बैलेंस एक बार बन गया — अब रोज़ सीखना नहीं पड़ता, बस लंबे समय बाद कभी-कभार चलाने से याद रहता है। पढ़ाई में: सवाल देखते ही तरीका खुद दिमाग में आ जाए।",
            whyNeeded = "यह आपका कीमती समय बचाता है ताकि आप तैयार चीज़ों को बार-बार न पढ़ते रहें।",
            withoutIt = "आप उन्हीं पक्के या आसान टॉपिक्स को दोबारा पढ़कर अपना समय गँवा देंगे।"
        ),
        StatusGuideItem(
            key = "Medium",
            name = "Practice",
            subtitle = "अभ्यास चाहिए / हाथ साफ़ करना है",
            whatItDoes = "यह बताता है कि कॉन्सेप्ट समझ तो आ गया है, लेकिन स्पीड, सटीकता (Accuracy) या आत्मविश्वास अभी कम है। यहाँ दोबारा लेक्चर नहीं देखना है, सिर्फ़ सवाल लगाने हैं।",
            example = "जैसे कार चलाने के नियम और गियर समझ आ गए, पर भीड़ वाली सड़क पर स्मूद चलाने के लिए अभी और ड्राइविंग करनी बाकी है। पढ़ाई में: तरीका पता है, पर हल करते समय समय ज़्यादा लगता है या छोटी गलती हो जाती है।",
            whyNeeded = "यह आपको बार-बार थ्योरी पढ़ने के जाल से निकालकर सीधे प्रैक्टिस की तरफ़ मोड़ता है।",
            withoutIt = "आप समझे हुए टॉपिक का भी पूरा वीडियो या नोट्स फिर से पढ़ने लगेंगे, जबकि ज़रूरत सिर्फ़ सवाल हल करने की थी।"
        ),
        StatusGuideItem(
            key = "Hard",
            name = "Relearn",
            subtitle = "फिर से सीखना है / जड़ से समझना है",
            whatItDoes = "यह एक अलार्म की तरह काम करता है जो बताता है कि कॉन्सेप्ट की नींव कच्ची है या पूरा भूल चुके हैं — इसे दोबारा शुरुआत से सीखना पड़ेगा।",
            example = "जैसे किसी रास्ते का नक्शा ही दिमाग से निकल गया हो, तो अंदाज़े से चलने की बजाय दोबारा मैप खोलकर पूरा रास्ता देखना पड़ता है। पढ़ाई में: सवाल पढ़कर ही समझ न आए कि शुरू कहाँ से करें।",
            whyNeeded = "यह आपकी सबसे कमज़ोर कड़ियों को तुरंत पकड़कर ऊपर लाता है।",
            withoutIt = "आप बिना बेसिक समझे सीधे सवाल लगाने बैठेंगे, बार-बार अटकेंगे और बेवजह तनाव होगा।"
        )
    )

    val noteLevelGuides: Map<String, StatusGuideItem> = linkedMapOf(
        "easy" to StatusGuideItem(
            key = "easy",
            name = "Easy",
            subtitle = "आसान — सीधा और 1-स्टेप",
            whatItDoes = "ऐसे बेसिक नियम या सीधे सवाल अलग रखता है जो बिना किसी घुमाव के एक ही स्टेप में हल हो जाते हैं।",
            example = "सीधी खाली सड़क पर चलना। (पढ़ाई में: जैसे 200 का 10% कितना होगा?)",
            whyNeeded = "किसी भी टॉपिक की नींव (Foundation) की जाँच और तुरंत याद दिलाने के लिए।",
            withoutIt = "बेसिक बातें और शुरुआती नियम कठिन नोट्स के बीच दब जाएँगे।"
        ),
        "medium" to StatusGuideItem(
            key = "medium",
            name = "Medium",
            subtitle = "मध्यम — सामान्य परीक्षा स्तर",
            whatItDoes = "ऐसे स्टैंडर्ड सवालों को चिह्नित करता है जिनमें 2–3 स्टेप लगते हैं या सवाल की भाषा थोड़ी घुमाकर दी होती है।",
            example = "हल्के मोड़ वाली शहर की सड़क। (पढ़ाई में: जैसे कीमत पहले 10% बढ़ी फिर 10% घटी, तो कुल असर क्या हुआ?)",
            whyNeeded = "परीक्षा का सबसे बड़ा हिस्सा इसी स्तर का होता है, इसलिए रोज़ के मुख्य अभ्यास का स्तर तय रहता है।",
            withoutIt = "आपको पता नहीं चलेगा कि सामान्य परीक्षा के लिए मुख्य अभ्यास किन सवालों पर करना है।"
        ),
        "hard" to StatusGuideItem(
            key = "hard",
            name = "Hard",
            subtitle = "कठिन — एक ही कॉन्सेप्ट, पर भारी",
            whatItDoes = "उन सवालों को अलग करता है जिनमें विषय तो एक ही है, लेकिन गहराई, लंबी गणना (Calculation) या गहरा लॉजिक ज़्यादा लगता है।",
            example = "सीधी लेकिन बहुत खड़ी पहाड़ी चढ़ाई — रास्ता एक ही है, पर दम ज़्यादा लगता है। (पढ़ाई में: किसी एक ही चैप्टर का बहुत लंबा या गहरा सवाल।)",
            whyNeeded = "यह दिमाग की क्षमता (Stamina) और गहराई से सोचने की ताकत बढ़ाता है।",
            withoutIt = "परीक्षा में थोड़ा भी भारी सवाल आते ही स्पीड टूट जाएगी और समय कम पड़ जाएगा।"
        ),
        "complex" to StatusGuideItem(
            key = "complex",
            name = "Complex",
            subtitle = "जटिल / पेचीदा — कई चीज़ों का मेल",
            whatItDoes = "ऐसे सवालों को पहचानता है जो सिर्फ़ भारी नहीं, बल्कि आपस में उलझे हुए हैं — जहाँ 2 या उससे ज़्यादा अलग-अलग कॉन्सेप्ट या शर्तें एक साथ जुड़ जाती हैं।",
            difference = "फर्क: Hard में एक ही चीज़ भारी होती है; Complex में कई हिस्से आपस में जुड़े होते हैं जिन्हें टुकड़ों में तोड़ना पड़ता है।",
            example = "बड़ा ट्रैफ़िक चौराहा जहाँ सिग्नल, मोड़, पैदल लोग और रफ़्तार सब एक साथ संभालने हों। (पढ़ाई में: एक ही सवाल में Ratio, Percentage और Mixture तीनों का नियम एक साथ लग जाए।)",
            whyNeeded = "यह दिमाग को बड़े और उलझे सवाल को छोटे हिस्सों में तोड़कर हल करने की ट्रेनिंग देता है।",
            withoutIt = "मिक्स्ड कॉन्सेप्ट वाला सवाल देखते ही दिमाग उलझ जाएगा और समझ नहीं आएगा कि कौन-सी शर्त पहले लगाएँ।"
        ),
        "expert" to StatusGuideItem(
            key = "expert",
            name = "Expert",
            subtitle = "विशेषज्ञ स्तर — सबसे ऊँचा / आउट-ऑफ-द-बॉक्स",
            whatItDoes = "उन चुनिंदा टॉप-लेवल या नए पैटर्न वाले सवालों को अलग सहेजता है जहाँ रटा हुआ फॉर्मूला फेल हो जाता है और कॉन्सेप्ट की सबसे गहरी समझ या स्मार्ट दृष्टिकोण चाहिए होता है।",
            example = "शतरंज के ग्रैंडमास्टर की चाल या रेसिंग ट्रैक पर ड्राइविंग। (पढ़ाई में: ऐसा सवाल जिसे ज़्यादातर लोग छोड़ देते हैं, पर असली लॉजिक पकड़ में आते ही 30 सेकंड में हल हो जाता है।)",
            whyNeeded = "यह आपको टॉप रैंक और सबसे कठिन पेपर के लिए मानसिक रूप से तैयार रखता है।",
            withoutIt = "परीक्षा से पहले रिवीज़न के लिए सबसे कीमती \"ट्रिकी सवाल\" सामान्य नोट्स की भीड़ में खो जाएँगे।"
        )
    )
}
