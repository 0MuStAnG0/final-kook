package com.example.data

import androidx.compose.runtime.mutableStateListOf

object MockData {
    val currentUser = User(
        id = "u_me",
        username = "kook_official",
        name = "فروشگاه مرکزی و دپارتمان مد کوک",
        avatarUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=400&q=80",
        bannerUrl = "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?auto=format&fit=crop&w=1080&q=80",
        bio = "مرکز تخصصی برندهای معتبر پوشاک، کیف، کفش و اکسسوری چرم ایرانی و بین‌المللی 👔👠 | ضمانت اصالت فیزیکی و پرو حضوری در شعب منتخب",
        rating = 4.95f,
        followersCount = 185000,
        followingCount = 42,
        isVerified = true,
        isSeller = true,
        storeCategory = "پوشاک، کیف و کفش",
        location = StoreLocation(
            latitude = 35.7830,
            longitude = 51.3750,
            province = "تهران",
            city = "تهران",
            neighborhood = "سعادت‌آباد",
            address = "تهران، سعادت‌آباد، میدان کاج، مجتمع تجاری سروستان، طبقه همکف، دپارتمان کوک",
            postalCode = "۱۹۹۷۸۱۴۵۲۳"
        ),
        openHours = "۱۰:۰۰ الی ۲۳:۰۰",
        phone = "۰۲۱-۲۲۰۸۹۳۴۱"
    )

    val users = listOf(
        currentUser,
        User(
            id = "brand_novin_leather",
            username = "novinleather_official",
            name = "نوین چرم | Novin Leather",
            avatarUrl = "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=400&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=1080&q=80",
            bio = "تولیدکننده پیشرو انواع کیف، کفش، کاپشن و کمربند چرم ۱۰۰٪ طبیعی گاوی و گوسفندی با استانداردهای صادراتی 🧥💼",
            rating = 4.9f,
            followersCount = 340000,
            followingCount = 18,
            isLive = true,
            isSeller = true,
            isVerified = true,
            storeCategory = "کیف، کفش و چرم طبیعی",
            location = StoreLocation(
                latitude = 35.7950,
                longitude = 51.4250,
                province = "تهران",
                city = "تهران",
                neighborhood = "الهیه / فرشته",
                address = "تهران، خیابان فرشته، مرکز خرید سام سنتر، طبقه اول، شعبه مرکزی نوین چرم",
                postalCode = "۱۹۶۵۸۷۴۱۲۰"
            ),
            openHours = "۱۰:۰۰ الی ۲۲:۳۰",
            phone = "۰۲۱-۲۲۰۵۹۹۸۸"
        ),
        User(
            id = "brand_hakopian",
            username = "hakopian_official",
            name = "هاکوپیان | HAKOPIAN",
            avatarUrl = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=400&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&w=1080&q=80",
            bio = "هنر اصیل خیاطی صنعتی، کت و شلوار مجلسی و کژوال، پالتوهای پشمی ماهوت با الیاف طبیعی و تن‌خور اختصاصی 👔",
            rating = 4.95f,
            followersCount = 420000,
            followingCount = 12,
            isLive = false,
            isSeller = true,
            isVerified = true,
            storeCategory = "کت و شلوار و پوشاک مردانه",
            location = StoreLocation(
                latitude = 35.7650,
                longitude = 51.4180,
                province = "تهران",
                city = "تهران",
                neighborhood = "میرداماد / جردن",
                address = "تهران، بلوار میرداماد، تقاطع آفریقا، پلاک ۳۱۰، خانه مد هاکوپیان",
                postalCode = "۱۹۱۷۸۴۵۶۱۲"
            ),
            openHours = "۰۹:۳۰ الی ۲۱:۳۰",
            phone = "۰۲۱-۸۸۷۷۹۹۰۰"
        ),
        User(
            id = "brand_maral_leather",
            username = "maralleather_group",
            name = "مارال چرم | Maral Leather",
            avatarUrl = "https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=400&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=1080&q=80",
            bio = "لبخند ماندگار با چرم طبیعی؛ ارائه‌دهنده بوت، نیم‌بوت، پوتین و کیف‌های دستی و دوشی لوکس زنانه و مردانه 🥾👜",
            rating = 4.85f,
            followersCount = 280000,
            followingCount = 25,
            isLive = true,
            isSeller = true,
            isVerified = true,
            storeCategory = "کیف و کفش چرم",
            location = StoreLocation(
                latitude = 35.8050,
                longitude = 51.4280,
                province = "تهران",
                city = "تهران",
                neighborhood = "تجریش",
                address = "تهران، میدان تجریش، مجتمع تجاری تندیس، طبقه دوم، پلاک ۸۲",
                postalCode = "۱۹۳۴۸۱۱۲۳۴"
            ),
            openHours = "۱۰:۰۰ الی ۲۲:۰۰",
            phone = "۰۲۱-۲۲۷۴۸۸۰۰"
        ),
        User(
            id = "brand_lcman",
            username = "lcman_fashion",
            name = "ال سی من | LC MAN",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=1080&q=80",
            bio = "طراحی مدرن اروپایی و تولید پوشاک کژوال، پیراهن‌های بدون چروک نخی و تیشرت‌های پنبه‌ای گلدوزی‌شده 👕",
            rating = 4.8f,
            followersCount = 195000,
            followingCount = 30,
            isLive = false,
            isSeller = true,
            isVerified = true,
            storeCategory = "پوشاک کژوال و پیراهن",
            location = StoreLocation(
                latitude = 35.7980,
                longitude = 51.4650,
                province = "تهران",
                city = "تهران",
                neighborhood = "اندرزگو / فرمانیه",
                address = "تهران، بلوار اندرزگو، مجتمع تجاری سانا، همکف شمالی، فروشگاه ال‌سی من",
                postalCode = "۱۹۵۴۸۷۶۳۲۱"
            ),
            openHours = "۱۰:۳۰ الی ۲۲:۳۰",
            phone = "۰۲۱-۲۶۶۵۷۷۸۸"
        ),
        User(
            id = "brand_melli_shoes",
            username = "melli_shoes_co",
            name = "گروه صنعتی کفش ملی | Melli",
            avatarUrl = "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=400&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1556906781-9a412961c28c?auto=format&fit=crop&w=1080&q=80",
            bio = "نامی ماندگار در صنعت کفش ایران؛ تولید تخصصی کفش‌های طبی، چرم راحتی، کتانی‌های پیاده‌روی و صندل‌های دست‌دوز 👞",
            rating = 4.75f,
            followersCount = 160000,
            followingCount = 15,
            isLive = false,
            isSeller = true,
            isVerified = true,
            storeCategory = "کفش طبی و اسپرت",
            location = StoreLocation(
                latitude = 35.7000,
                longitude = 51.4050,
                province = "تهران",
                city = "تهران",
                neighborhood = "انقلاب / فردوسی",
                address = "تهران، خیابان فردوسی، بالاتر از کوشک، پلاک ۱۸۰، فروشگاه مرکزی کفش ملی",
                postalCode = "۱۱۳۵۸۹۷۴۱۲"
            ),
            openHours = "۰۹:۰۰ الی ۲۱:۰۰",
            phone = "۰۲۱-۶۶۷۰۲۲۳۳"
        ),
        User(
            id = "brand_body_spinner",
            username = "bodyspinner_iran",
            name = "بادی اسپینر | Body Spinner",
            avatarUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=400&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1487222477894-8943e31ef7b2?auto=format&fit=crop&w=1080&q=80",
            bio = "روایتگر استایل آزاد و جوان‌پسند؛ انواع کاپشن بامبر، شلوارهای کتان و جین، هودی‌های بیسیک و استایل خیابانی 🧥✨",
            rating = 4.88f,
            followersCount = 310000,
            followingCount = 40,
            isLive = true,
            isSeller = true,
            isVerified = true,
            storeCategory = "استریت‌استایل و جین",
            location = StoreLocation(
                latitude = 35.7560,
                longitude = 51.3710,
                province = "تهران",
                city = "تهران",
                neighborhood = "شهرک غرب",
                address = "تهران، شهرک غرب، خیابان ایران‌زمین، مرکز خرید گلستان، طبقه اول، واحد ۱۲۴",
                postalCode = "۱۴۶۵۸۹۷۴۱۲"
            ),
            openHours = "۱۰:۰۰ الی ۲۳:۰۰",
            phone = "۰۲۱-۸۸۰۸۶۶۵۵"
        ),
        User(
            id = "brand_dorian_leather",
            username = "doriangroup_luxury",
            name = "چرم درسا | Dorsa Group",
            avatarUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=400&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?auto=format&fit=crop&w=1080&q=80",
            bio = "خلق سبک زندگی لوکس ایرانی؛ کیف‌های مجلسی زنانه چرم سنگی، اکسسوری‌های مینیمال و کفش‌های کلاسیک مجلل 👜💎",
            rating = 4.96f,
            followersCount = 480000,
            followingCount = 10,
            isLive = false,
            isSeller = true,
            isVerified = true,
            storeCategory = "کیف، کفش و اکسسوری لوکس",
            location = StoreLocation(
                latitude = 35.7620,
                longitude = 51.4640,
                province = "تهران",
                city = "تهران",
                neighborhood = "پاسداران",
                address = "تهران، خیابان پاسداران، برج سفید، طبقه همکف تجاری، بوتیک درسا",
                postalCode = "۱۹۵۸۷۴۱۲۳۰"
            ),
            openHours = "۱۰:۰۰ الی ۲۲:۰۰",
            phone = "۰۲۱-۲۲۵۵۹۹۰۰"
        )
    )

    val posts = mutableStateListOf(
        Post(
            id = "post_novin_1",
            author = users[1], // نوین چرم
            description = "کالکشن پاییز و زمستان نوین چرم رونمایی شد ✨ کاپشن چرم طبیعی ۱۰۰٪ گاوی با دوخت پریمیوم و استانداردهای صادراتی. تیزر رسمی با حضور بهرام رادان. امکان تست سایز و پرو حضوری در کلیه شعب سام‌سنتر، فرشته و تجریش با ارسال رایگان سراسر کشور. 🧥⚡ #نوین_چرم #کاپشن_چرم #چرم_طبیعی",
            likesCount = 28400,
            commentsCount = 890,
            sharesCount = 3400,
            savesCount = 5200,
            product = Product(
                id = "prod_novin_jacket",
                title = "کاپشن چرم طبیعی مردانه مدل خلبانی نوین چرم",
                price = 14800000,
                oldPrice = 17500000,
                imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=1080&q=80",
                inStock = true
            ),
            imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=1080&q=80",
            audioTrack = "موسیقی رسمی تیزر نوین چرم (بهرام رادان)",
            videoUrl = "https://aspb3.cdn.asset.aparat.com/aparat-video/792fd47f408182d4c238bc477d7eedd51546393.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6IjMwNzlkNDAwMWQ2YjdjOWZmZWUxYzFkOTEyMWUzYTE4IiwiZXhwIjoxNzkwODEzMjgwLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9._XgAs9cUJbSskecPvWYRPAZO_qmr2QCzyL1qHCOjRGU"
        ),
        Post(
            id = "post_hakopian_1",
            author = users[2], // هاکوپیان
            description = "شکوه وقار و اصالت با کت و شلوار دست‌دوز و صنعتی هاکوپیان 👔 دوخته شده از مرغوب‌ترین پارچه پشمی سوپر ۱۲۰ ایتالیا با آستر کوپرو ضدتعریق. تیزر رسمی هنر خیاطی و دوخت اختصاصی خانه مد هاکوپیان. #هاکوپیان #کت_شلوار #استایل_رسمی",
            likesCount = 34500,
            commentsCount = 1120,
            sharesCount = 4200,
            savesCount = 6800,
            product = Product(
                id = "prod_hakopian_suit",
                title = "کت و شلوار کلاسیک ماهوت سرمه‌ای هاکوپیان",
                price = 21500000,
                oldPrice = 25000000,
                imageUrl = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=1080&q=80",
                inStock = true
            ),
            imageUrl = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=1080&q=80",
            audioTrack = "موسیقی سمفونیک تیزر هاکوپیان",
            videoUrl = "https://aspb12.cdn.asset.aparat.com/aparat-video/b80a7d31efd23e0a81dee144352eb8773339802-576p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6IjZiNTY4ZDYwMTVmZGZhZmFmYTEwYWQxMjg3ZDZjYzhlIiwiZXhwIjoxNzkwODEzNjgxLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.WkPMV9UrevnROew4NJ2ynU1NWYbkyhL7Ad-A4X7o-eE"
        ),
        Post(
            id = "post_maral_1",
            author = users[3], // مارال چرم
            description = "کالکشن تخصصی بوت، نیم‌بوت و کفش‌های چرم دست‌دوز مارال چرم 🥾 تیزر رسمی معرفی جشنواره با تخفیف‌های ویژه سراسری و گارانتی اصالت ۲ ساله چرم ایرانی. #مارال_چرم #بوت_زنانه #کفش_چرم",
            likesCount = 19200,
            commentsCount = 540,
            sharesCount = 1800,
            savesCount = 3900,
            product = Product(
                id = "prod_maral_boot",
                title = "بوت بنددار چرم نبوک زنانه مارال چرم",
                price = 5600000,
                oldPrice = 6900000,
                imageUrl = "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=1080&q=80",
                inStock = true
            ),
            imageUrl = "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=1080&q=80",
            audioTrack = "تیزر رسمی جشنواره مارال چرم",
            videoUrl = "https://aspb12.cdn.asset.aparat.com/aparat-video/46d92e4ab5c552061683c6770fa0c91824273676-480p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6ImY3YmE5ZjBjOTkwM2ZkMzdlMzJmMDc2M2YyNDgxYjVkIiwiZXhwIjoxNzkwODEzMjk1LCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.fZkf_J3XaVQjnq3UaAvWjkm3Z_tZwZVVgd7xsFUCj9M"
        ),
        Post(
            id = "post_dorsa_1",
            author = users[6], // چرم درسا
            description = "شاهکاری از تلفیق چرم طبیعی و طلا 👜 تیزر رسمی معرفی گروه محصولات لوکس چرم درسا و زیورآلات کالکشن دیبا. همراه با بسته‌بندی هاردباکس و شناسنامه اصالت. #درسا #کیف_زنانه #لاکچری",
            likesCount = 41200,
            commentsCount = 1450,
            sharesCount = 6100,
            savesCount = 8900,
            product = Product(
                id = "prod_dorsa_bag",
                title = "کیف چرم سنگی مجلسی زنانه درسا کالکشن دیبا",
                price = 9800000,
                oldPrice = 11500000,
                imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=1080&q=80",
                inStock = true
            ),
            imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=1080&q=80",
            audioTrack = "تیزر معرفی محصولات گروه چرم درسا",
            videoUrl = "https://aspb2.cdn.asset.aparat.com/aparat-video/538437c229faa347b6af34423ca000c17346904-480p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6ImZmNDFiZjNiYmJmOGE3YjEzNjk0ZmM1ZDRlNGJkNjZiIiwiZXhwIjoxNzkwODEzMjgxLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.3PT5lvXABszAMREc3hKrOQA6I_gNz2HD-mmGGLIuSOs"
        ),
        Post(
            id = "post_bodyspinner_1",
            author = users[5], // بادی اسپینر
            description = "کالکشن جدید پاییز و زمستان بادی اسپینر 🧥🌧️ تیزر رسمی استریت‌استایل تن‌ریس؛ معرفی کاپشن پافر اورسایز ضدآب، هودی و سویشرت‌های مدرن ایرانی با طراحی روز جهانی. #بادی_اسپینر #پافر #استایل_خیابانی",
            likesCount = 31200,
            commentsCount = 890,
            sharesCount = 3800,
            savesCount = 5900,
            product = Product(
                id = "prod_bodyspinner_puffer",
                title = "کاپشن پافر ضدآب کلاه‌دار مشکی مات بادی اسپینر",
                price = 4950000,
                oldPrice = 6200000,
                imageUrl = "https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=1080&q=80",
                inStock = true
            ),
            imageUrl = "https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=1080&q=80",
            audioTrack = "تیزر رسمی کالکشن پاییز زمستان بادی اسپینر",
            videoUrl = "https://caspian25.cdn.asset.aparat.com/aparat-video/0e4bc1d6a27c09813fe647af2631579a67225294-480p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6ImY3ZDBkYzYxYTk5NTFhZWIyOThmYzMzYjEyMzFkYTA4IiwiZXhwIjoxNzkwODEzMjg4LCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.30jMAf5YuCEP69mA1pxOxvEi1fmX2EvBmczWWR00GJU"
        ),
        Post(
            id = "post_lcman_1",
            author = users[4], // ال سی من
            description = "«بذار تنت انتخاب کنه!» 🧥 تیزر تبلیغاتی رسمی برند ال سی من (LC MAN) با حضور محمدرضا گلزار؛ کلکسیون پالتوهای فوتر پشمی، کت و پیراهن‌های کژوال بدون چروک. #ال_سی_من #پالتو_مردانه #محمدرضا_گلزار",
            likesCount = 38800,
            commentsCount = 1220,
            sharesCount = 4950,
            savesCount = 6800,
            product = Product(
                id = "prod_lcman_coat",
                title = "پالتو فوتر پشمی دو دکمه طوسی ال سی من",
                price = 8200000,
                oldPrice = 9600000,
                imageUrl = "https://images.unsplash.com/photo-1544923246-77307dd654cb?auto=format&fit=crop&w=1080&q=80",
                inStock = true
            ),
            imageUrl = "https://images.unsplash.com/photo-1544923246-77307dd654cb?auto=format&fit=crop&w=1080&q=80",
            audioTrack = "موسیقی تبلیغاتی رسمی ال سی من (بذار تنت انتخاب کنه)",
            videoUrl = "https://aspb21.cdn.asset.aparat.com/aparat-video/7204eca46a062bc7ab4108c7d11055f428084822-480p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6ImNiYWY4MTdiZmY0NTYxNDc0OWNlNmQ4NDIwMTE0M2Q4IiwiZXhwIjoxNzkwODEzMjgxLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.EzFwhfAR-55tYfXBrL5WVnzWM7FZaMp_GBdafv7xly8"
        ),
        Post(
            id = "post_melli_1",
            author = users[5], // کفش ملی
            description = "گام‌هایی استوار و ماندگار با کفش ملی 👞 تیزر تبلیغاتی رسمی گروه صنعتی کفش ملی؛ تولید تخصصی کفش‌های چرم طبی، اداری و راحتی با کفی مموری فوم ارگونومیک. #کفش_ملی #کفش_طبی #چرم_طبیعی",
            likesCount = 22400,
            commentsCount = 680,
            sharesCount = 2100,
            savesCount = 4100,
            product = Product(
                id = "prod_melli_oxford",
                title = "کفش مردانه چرم طبیعی مدل آکسفورد طبی کفش ملی",
                price = 3850000,
                oldPrice = 4600000,
                imageUrl = "https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?auto=format&fit=crop&w=1080&q=80",
                inStock = true
            ),
            imageUrl = "https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?auto=format&fit=crop&w=1080&q=80",
            audioTrack = "تیزر تلویزیونی رسمی کفش ملی",
            videoUrl = "https://aspb2.cdn.asset.aparat.com/aparat-video/9c28b21dd8dfc406ce15e1e76a33b8064271147-720p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6IjljMjY5ZjJkMGQ2NzU0MWRkOGM5M2M4MTM2MzNhMGYzIiwiZXhwIjoxNzkwODEzMjgyLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.s8-NjocAY7zhNbFkd6A0cgcCl5zwuR1ZQqOO2noeLSQ"
        ),
        Post(
            id = "post_novin_2",
            author = users[1], // نوین چرم ۲
            description = "تیزر دوم کالکشن بهاره نوین چرم با هنرنمایی بهرام رادان 💼 کیف‌های اداری، دیپلمات و اکسسوری چرم طبیعی گاوی درجه یک دست‌دوز. #نوین_چرم #کیف_اداری #کیف_چرم",
            likesCount = 24600,
            commentsCount = 590,
            sharesCount = 2400,
            savesCount = 4100,
            product = Product(
                id = "prod_novin_briefcase",
                title = "کیف دیپلمات چرم گاوی طرح ناپا نوین چرم",
                price = 7900000,
                oldPrice = 9200000,
                imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=1080&q=80",
                inStock = true
            ),
            imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=1080&q=80",
            audioTrack = "تیزر نوین چرم و بهرام رادان - زمستان",
            videoUrl = "https://aspb1.cdn.asset.aparat.com/aparat-video/2f1b98ff743785f63fff53d73b9c301d2366839-360p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6IjQ5NWQyYzE5ODM5NDBjODQzNjNmN2I3NjA2ZDZhNzU3IiwiZXhwIjoxNzkwODEzMjgyLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.k3_vpyEJIQIgBzGzWYvoc2MVxpK0CIdXd0lmLqIKJHU"
        )
    )

    val profilePosts = mutableStateListOf(
        posts[0].copy(id = "p1_me", author = currentUser),
        posts[1].copy(id = "p2_me", author = currentUser),
        posts[2].copy(id = "p3_me", author = currentUser),
        posts[3].copy(id = "p4_me", author = currentUser),
        posts[4].copy(id = "p5_me", author = currentUser),
        posts[5].copy(id = "p6_me", author = currentUser),
        posts[6].copy(id = "p7_me", author = currentUser),
        posts[7].copy(id = "p8_me", author = currentUser)
    )

    val sampleStoreOrders = mutableStateListOf<StoreOrder>()

    fun addPost(post: Post) {
        posts.add(0, post)
        profilePosts.add(0, post)
    }

    fun addStoreOrder(order: StoreOrder) {
        sampleStoreOrders.add(0, order)
    }
}
