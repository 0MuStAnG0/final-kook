import 'package:flutter/material.dart';
import '../models/models.dart';
import '../services/api_service.dart';

enum UserMode {
  customer('حالت خریدار', 'سبد خرید'),
  store('حالت فروشگاه', 'سفارشات');

  final String title;
  final String tabLabel;
  const UserMode(this.title, this.tabLabel);
}

class AppStateManager extends ChangeNotifier {
  final ApiService api = ApiService();

  bool isServerConnected = true;
  bool isDarkMode = true;
  UserMode currentUserMode = UserMode.customer;

  // Default Profiles
  final User defaultCustomer = User(
    id: 'user_me_01',
    username: 'sahar_rad',
    name: 'سحر رادمان',
    gmail: 'sahar.radman@gmail.com',
    phone: '۰۹۱۲۳۴۵۶۷۸۹',
    city: 'تهران',
    interests: 'علاقه‌مند به استایل کژوال، مد پایدار، اکسسوری‌های مینیمال',
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80',
    bannerUrl: 'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=1080&q=80',
    bio: 'طراح گرافیک و شیفته مد روز ✨ | ساکن تهران | دوستدار استایل‌های ساده و باکیفیت',
    rating: 5.0,
    followersCount: 1420,
    followingCount: 88,
    isVerified: true,
    isSeller: false,
    accountType: AccountType.regularUser,
  );

  final User defaultStore = User(
    id: 'u_me',
    username: 'kook_style',
    name: 'بوتیک کوک استایل',
    gmail: 'store@kook.orangeit.site',
    phone: '۰۲۱-۲۲۰۸۹۳۴۱',
    city: 'تهران',
    avatarUrl: 'https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?auto=format&fit=crop&w=400&q=80',
    bannerUrl: 'https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?auto=format&fit=crop&w=1080&q=80',
    bio: 'جدیدترین کالکشن‌های پوشاک و استایل خیابانی 👗🧥 | تایید شده رسمی اصناف Orange IT | ارسال فوری 🚚',
    rating: 4.9,
    followersCount: 45800,
    followingCount: 120,
    isVerified: true,
    isSeller: true,
    accountType: AccountType.storeOwner,
    storeCategory: 'پوشاک و مد',
    location: StoreLocation(
      latitude: 35.7830,
      longitude: 51.3750,
      province: 'تهران',
      city: 'تهران',
      neighborhood: 'سعادت‌آباد',
      address: 'تهران، سعادت‌آباد، میدان کاج، مجتمع تجاری سروستان، طبقه همکف، واحد ۱۲',
    ),
    openHours: '۱۰:۰۰ الی ۲۲:۳۰',
    phoneOffice: '۰۲۱-۲۲۰۸۹۳۴۱',
    storeApplication: StoreApplication(
      id: 'app_approved_01',
      storeName: 'بوتیک کوک استایل',
      category: 'پوشاک و مد',
      city: 'تهران',
      address: 'تهران، سعادت‌آباد، میدان کاج، پاساژ سروستان',
      nationalId: '۰۰۱۲۳۴۵۶۷۸',
      documentTitle: 'پروانه کسب معتبر اتحادیه صنف پوشاک',
      description: 'ارائه البسه باکیفیت و ترند روز با امکان پرو حضوری',
      submittedAt: '۱۴۰۳/۰۵/۱۰',
      status: StoreApplicationStatus.approved,
      adminFeedback: 'مدارک و محل کسب احراز هویت شد و مورد تایید است.',
    ),
  );

  late User currentUser;
  int walletBalance = 8450000;
  int rewardCoins = 320;

  // Posts Feed List
  List<Post> posts = [];
  List<MarketOrder> orders = [];
  List<TryOnBooking> tryOnBookings = [];
  List<ChatMessage> chatMessages = [];

  AppStateManager() {
    currentUser = defaultCustomer;
    _initializeData();
  }

  void _initializeData() {
    // Initial Mock Posts - Professional Clothing & Footwear Brands
    posts = [
      Post(
        id: 'post_novin_1',
        author: User(
          id: 'brand_novin_leather',
          username: 'novinleather_official',
          name: 'نوین چرم | Novin Leather',
          avatarUrl: 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=400&q=80',
          bannerUrl: 'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=1080&q=80',
          bio: 'تولیدکننده پیشرو کیف، کفش، کاپشن و کمربند چرم ۱۰۰٪ طبیعی با استانداردهای صادراتی 🧥💼',
          rating: 4.9,
          followersCount: 340000,
          followingCount: 18,
          isVerified: true,
          isSeller: true,
          storeCategory: 'کیف، کفش و چرم طبیعی',
        ),
        description: 'کالکشن جدید پاییز و زمستان نوین چرم رونمایی شد ✨ کاپشن چرم طبیعی گاوی ۱۰۰٪ مدل خلبانی با دوخت فوق‌العاده مقاوم، زیپ‌های YKK اصل ژاپن و آستر کشباف متراکم. تیزر رسمی با حضور بهرام رادان. امکان تست سایز و پرو حضوری در کلیه شعب سام‌سنتر و تجریش. ارسال رایگان سراسر کشور!',
        likesCount: 28400,
        commentsCount: 890,
        sharesCount: 3400,
        savesCount: 5200,
        imageUrl: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=800&q=80',
        videoUrl: 'https://aspb3.cdn.asset.aparat.com/aparat-video/792fd47f408182d4c238bc477d7eedd51546393.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6IjMwNzlkNDAwMWQ2YjdjOWZmZWUxYzFkOTEyMWUzYTE4IiwiZXhwIjoxNzkwODEzMjgwLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9._XgAs9cUJbSskecPvWYRPAZO_qmr2QCzyL1qHCOjRGU',
        audioTrack: 'موسیقی رسمی تیزر نوین چرم (بهرام رادان)',
        product: Product(
          id: 'prod_novin_jacket',
          title: 'کاپشن چرم طبیعی گاوی خلبانی نوین چرم',
          price: 14800000,
          oldPrice: 17500000,
          category: 'پوشاک و مد',
          inStock: true,
          imageUrl: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=800&q=80',
        ),
      ),
      Post(
        id: 'post_hakopian_1',
        author: User(
          id: 'brand_hakopian',
          username: 'hakopian_official',
          name: 'هاکوپیان | HAKOPIAN',
          avatarUrl: 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=400&q=80',
          bannerUrl: 'https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&w=1080&q=80',
          bio: 'هنر اصیل خیاطی صنعتی، کت و شلوار مجلسی و کژوال با الیاف ۱۰۰٪ طبیعی 👔',
          rating: 4.95,
          followersCount: 420000,
          followingCount: 12,
          isVerified: true,
          isSeller: true,
          storeCategory: 'کت و شلوار و پوشاک مردانه',
        ),
        description: 'وقار و اصالت با کت و شلوار کلاسیک ماهوت هاکوپیان 👔 دوخته شده از پارچه پشمی سوپر ۱۲۰ ایتالیا با آستر کوپرو ضدتعریق و برش لیزری بی‌نقص. تیزر رسمی هنر خیاطی و دوخت اختصاصی خانه مد هاکوپیان.',
        likesCount: 34500,
        commentsCount: 1120,
        sharesCount: 4200,
        savesCount: 6800,
        imageUrl: 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80',
        videoUrl: 'https://aspb12.cdn.asset.aparat.com/aparat-video/b80a7d31efd23e0a81dee144352eb8773339802-576p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6IjZiNTY4ZDYwMTVmZGZhZmFmYTEwYWQxMjg3ZDZjYzhlIiwiZXhwIjoxNzkwODEzNjgxLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.WkPMV9UrevnROew4NJ2ynU1NWYbkyhL7Ad-A4X7o-eE',
        audioTrack: 'موسیقی سمفونیک تیزر هاکوپیان',
        product: Product(
          id: 'prod_hakopian_suit',
          title: 'کت و شلوار پشمی رسمی هاکوپیان سرمه‌ای',
          price: 21500000,
          oldPrice: 25000000,
          category: 'پوشاک و مد',
          inStock: true,
          imageUrl: 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80',
        ),
      ),
      Post(
        id: 'post_dorsa_1',
        author: User(
          id: 'brand_dorian_leather',
          username: 'doriangroup_luxury',
          name: 'چرم درسا | Dorsa Group',
          avatarUrl: 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=400&q=80',
          bannerUrl: 'https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?auto=format&fit=crop&w=1080&q=80',
          bio: 'خلق سبک زندگی لوکس ایرانی؛ کیف‌های مجلسی، اکسسوری و کفش‌های چرم سنگی 👜💎',
          rating: 4.96,
          followersCount: 480000,
          followingCount: 10,
          isVerified: true,
          isSeller: true,
          storeCategory: 'کیف، کفش و اکسسوری لوکس',
        ),
        description: 'شاهکاری از کالکشن دیبا: کیف دستی و دوشی چرم طبیعی سنگی درسا 👜 با یراق‌آلات طلایی مقاوم در برابر اکسیداسیون و بند دوشی زنجیری ارگونومیک. تیزر رسمی معرفی گروه محصولات لوکس درسا.',
        likesCount: 41200,
        commentsCount: 1450,
        sharesCount: 6100,
        savesCount: 8900,
        imageUrl: 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=800&q=80',
        videoUrl: 'https://aspb2.cdn.asset.aparat.com/aparat-video/538437c229faa347b6af34423ca000c17346904-480p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6ImZmNDFiZjNiYmJmOGE3YjEzNjk0ZmM1ZDRlNGJkNjZiIiwiZXhwIjoxNzkwODEzMjgxLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.3PT5lvXABszAMREc3hKrOQA6I_gNz2HD-mmGGLIuSOs',
        audioTrack: 'تیزر معرفی محصولات گروه چرم درسا',
        product: Product(
          id: 'prod_dorsa_bag',
          title: 'کیف دوشی چرم سنگی زنانه درسا کالکشن دیبا',
          price: 9800000,
          oldPrice: 11500000,
          category: 'پوشاک و مد',
          inStock: true,
          imageUrl: 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=800&q=80',
        ),
      ),
      Post(
        id: 'post_maral_1',
        author: User(
          id: 'brand_maral_leather',
          username: 'maralleather_group',
          name: 'مارال چرم | Maral Leather',
          avatarUrl: 'https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=400&q=80',
          bannerUrl: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=1080&q=80',
          bio: 'لبخند ماندگار با چرم طبیعی؛ بوت، نیم‌بوت و کفش‌های چرم دست‌دوز 🥾👠',
          rating: 4.85,
          followersCount: 280000,
          followingCount: 25,
          isVerified: true,
          isSeller: true,
          storeCategory: 'کیف و کفش چرم',
        ),
        description: 'بوت بنددار چرم طبیعی نبوک مارال چرم با عایق حرارتی میکروفایبر و زیره طبی پی‌یو ضد لغزش 🥾 تیزر رسمی جشنواره تخفیف‌های ویژه مارال چرم با ۲ سال خدمات پس از فروش.',
        likesCount: 19200,
        commentsCount: 540,
        sharesCount: 1800,
        savesCount: 3900,
        imageUrl: 'https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=800&q=80',
        videoUrl: 'https://aspb12.cdn.asset.aparat.com/aparat-video/46d92e4ab5c552061683c6770fa0c91824273676-480p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6ImY3YmE5ZjBjOTkwM2ZkMzdlMzJmMDc2M2YyNDgxYjVkIiwiZXhwIjoxNzkwODEzMjk1LCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.fZkf_J3XaVQjnq3UaAvWjkm3Z_tZwZVVgd7xsFUCj9M',
        audioTrack: 'تیزر رسمی جشنواره مارال چرم',
        product: Product(
          id: 'prod_maral_boot',
          title: 'بوت چرم نبوک زنانه مارال چرم مدل ترند',
          price: 5600000,
          oldPrice: 6900000,
          category: 'پوشاک و مد',
          inStock: true,
          imageUrl: 'https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=800&q=80',
        ),
      ),
      Post(
        id: 'post_melli_1',
        author: User(
          id: 'brand_melli_shoes',
          username: 'melli_shoes_co',
          name: 'گروه صنعتی کفش ملی | Melli',
          avatarUrl: 'https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=400&q=80',
          bannerUrl: 'https://images.unsplash.com/photo-1556906781-9a412961c28c?auto=format&fit=crop&w=1080&q=80',
          bio: 'نامی ماندگار در صنعت کفش؛ تولید تخصصی کفش‌های طبی اداری، اسپرت و راحتی 👞',
          rating: 4.75,
          followersCount: 160000,
          followingCount: 15,
          isVerified: true,
          isSeller: true,
          storeCategory: 'کفش طبی و اسپرت',
        ),
        description: 'کفش مردانه تمام چرم طبیعی آکسفورد کفش ملی با کفی طبی ارگونومیک مموری‌فوم 👞 تیزر تبلیغاتی تلویزیونی کفش ملی؛ کاهش‌دهنده خستگی ستون فقرات و فشار به زانو در ساعات کاری طولانی.',
        likesCount: 22400,
        commentsCount: 680,
        sharesCount: 2100,
        savesCount: 4100,
        imageUrl: 'https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?auto=format&fit=crop&w=800&q=80',
        videoUrl: 'https://aspb2.cdn.asset.aparat.com/aparat-video/9c28b21dd8dfc406ce15e1e76a33b8064271147-720p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6IjljMjY5ZjJkMGQ2NzU0MWRkOGM5M2M4MTM2MzNhMGYzIiwiZXhwIjoxNzkwODEzMjgyLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.s8-NjocAY7zhNbFkd6A0cgcCl5zwuR1ZQqOO2noeLSQ',
        audioTrack: 'تیزر تلویزیونی رسمی کفش ملی',
        product: Product(
          id: 'prod_melli_oxford',
          title: 'کفش مردانه چرم طبیعی آکسفورد طبی کفش ملی',
          price: 3850000,
          oldPrice: 4600000,
          category: 'پوشاک و مد',
          inStock: true,
          imageUrl: 'https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?auto=format&fit=crop&w=800&q=80',
        ),
      ),
      Post(
        id: 'post_bodyspinner_1',
        author: User(
          id: 'brand_body_spinner',
          username: 'bodyspinner_iran',
          name: 'بادی اسپینر | Body Spinner',
          avatarUrl: 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=400&q=80',
          bannerUrl: 'https://images.unsplash.com/photo-1487222477894-8943e31ef7b2?auto=format&fit=crop&w=1080&q=80',
          bio: 'روایتگر استایل آزاد و جوان‌پسند؛ انواع کاپشن بامبر، شلوارهای کتان و جین، پافر اورسایز 🧥✨',
          rating: 4.88,
          followersCount: 310000,
          followingCount: 40,
          isVerified: true,
          isSeller: true,
          storeCategory: 'استریت‌استایل و جین',
        ),
        description: 'کاپشن پافر کلاه‌دار اورسایز ضدآب بادی اسپینر 🌧️ تیزر رسمی کالکشن پاییز و زمستان بادی اسپینر؛ بسیار سبک و گرم با الیاف سیلیکونی پر شده با چگالی بالا و جیب‌های پلار حرارتی.',
        likesCount: 31200,
        commentsCount: 890,
        sharesCount: 3800,
        savesCount: 5900,
        imageUrl: 'https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=800&q=80',
        videoUrl: 'https://caspian25.cdn.asset.aparat.com/aparat-video/0e4bc1d6a27c09813fe647af2631579a67225294-480p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6ImY3ZDBkYzYxYTk5NTFhZWIyOThmYzMzYjEyMzFkYTA4IiwiZXhwIjoxNzkwODEzMjg4LCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.30jMAf5YuCEP69mA1pxOxvEi1fmX2EvBmczWWR00GJU',
        audioTrack: 'تیزر رسمی کالکشن پاییز زمستان بادی اسپینر',
        product: Product(
          id: 'prod_bodyspinner_puffer',
          title: 'پافر ضدآب کلاه‌دار اورسایز مشکی بادی اسپینر',
          price: 4950000,
          oldPrice: 6200000,
          category: 'پوشاک و مد',
          inStock: true,
          imageUrl: 'https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=800&q=80',
        ),
      ),
      Post(
        id: 'post_lcman_1',
        author: User(
          id: 'brand_lcman',
          username: 'lcman_fashion',
          name: 'ال سی من | LC MAN',
          avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80',
          bannerUrl: 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=1080&q=80',
          bio: 'طراحی مدرن اروپایی و تولید پوشاک کژوال، پالتو و پیراهن‌های نخی بدون چروک 👕🧥',
          rating: 4.8,
          followersCount: 195000,
          followingCount: 30,
          isVerified: true,
          isSeller: true,
          storeCategory: 'پوشاک کژوال و پیراهن',
        ),
        description: '«بذار تنت انتخاب کنه!» 🧥 تیزر تبلیغاتی رسمی برند ال سی من (LC MAN) با حضور محمدرضا گلزار؛ کلکسیون پالتوهای فوتر پشمی دو دکمه طوسی با طراحی اسلیم و کلاسیک.',
        likesCount: 38800,
        commentsCount: 1220,
        sharesCount: 4950,
        savesCount: 6800,
        imageUrl: 'https://images.unsplash.com/photo-1544923246-77307dd654cb?auto=format&fit=crop&w=800&q=80',
        videoUrl: 'https://aspb21.cdn.asset.aparat.com/aparat-video/7204eca46a062bc7ab4108c7d11055f428084822-480p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6ImNiYWY4MTdiZmY0NTYxNDc0OWNlNmQ4NDIwMTE0M2Q4IiwiZXhwIjoxNzkwODEzMjgxLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.EzFwhfAR-55tYfXBrL5WVnzWM7FZaMp_GBdafv7xly8',
        audioTrack: 'موسیقی تبلیغاتی رسمی ال سی من (بذار تنت انتخاب کنه)',
        product: Product(
          id: 'prod_lcman_coat',
          title: 'پالتو فوتر پشمی دو دکمه طوسی ال سی من',
          price: 8200000,
          oldPrice: 9600000,
          category: 'پوشاک و مد',
          inStock: true,
          imageUrl: 'https://images.unsplash.com/photo-1544923246-77307dd654cb?auto=format&fit=crop&w=800&q=80',
        ),
      ),
    ];

    // Orders
    orders = [
      MarketOrder(
        id: 'ORD-9842',
        productTitle: 'کت بلیزر اورسایز ماهوت طوسی',
        productImage: 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=800&q=80',
        price: 1850000,
        status: OrderStatus.shipped,
        orderDate: '۱۴۰۳/۰۶/۲۸',
        customerName: 'سحر رادمان',
        customerAddress: 'تهران، سعادت‌آباد، خیابان سرو غربی، پلاک ۲۴',
        trackingCode: '۲۹۸۴۷۳۸۲۹۱۰۰۳۹',
      ),
      MarketOrder(
        id: 'ORD-9843',
        productTitle: 'کیف دستی اداری چرم عسلی اصل',
        productImage: 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=800&q=80',
        price: 2950000,
        status: OrderStatus.delivered,
        orderDate: '۱۴۰۳/۰۶/۲۰',
        customerName: 'سحر رادمان',
        customerAddress: 'تهران، سعادت‌آباد، خیابان سرو غربی، پلاک ۲۴',
        trackingCode: '۱۹۸۷۴۶۵۷۲۳۸۹۱۱',
      ),
    ];

    // Try-On Bookings
    tryOnBookings = [
      TryOnBooking(
        id: 'TB-102',
        storeName: 'بوتیک کوک استایل',
        storeAvatar: 'https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?auto=format&fit=crop&w=400&q=80',
        productTitle: 'کت بلیزر اورسایز ماهوت طوسی',
        productImage: 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=800&q=80',
        scheduledDate: 'فردا - ساعت ۱۷:۰۰',
        scheduledTime: '۱۷:۰۰ تا ۱۸:۰۰',
        bookingCode: 'KOOK-PRO-984',
        storeAddress: 'سعادت‌آباد، مجتمع سروستان، واحد ۱۲',
        status: 'تایید شده و رزرو اتاق پرو',
      ),
    ];

    // Chat Messages
    chatMessages = [
      ChatMessage(
        id: 'msg_1',
        senderId: 'u_me',
        text: 'سلام وقت بخیر! در خدمتتون هستم، آیا برای انتخاب سایز کت بلیزر کمکی نیاز دارید؟',
        timestamp: '۱۰:۲۴',
        isFromMe: false,
      ),
      ChatMessage(
        id: 'msg_2',
        senderId: 'user_me_01',
        text: 'سلام، بله قد من ۱۶۸ هست، سایز ۳۸ مناسبمه یا ۴۰؟',
        timestamp: '۱۰:۲۶',
        isFromMe: true,
      ),
      ChatMessage(
        id: 'msg_3',
        senderId: 'u_me',
        text: 'چون کار به صورت اورسایز طراحی شده، سایز ۳۸ کاملاً ایده‌آله. براتون فاکتور با تخفیف ویژه صادر کردم:',
        timestamp: '۱۰:۲۸',
        isFromMe: false,
        invoice: Invoice(
          id: 'INV-7721',
          title: 'کت بلیزر اورسایز سایز ۳۸ + ارسال رایگان',
          amount: 1850000,
          description: 'ارسال با پست پیشتاز و بیمه کامل مرسوله',
        ),
      ),
    ];
  }

  void switchUserMode(UserMode mode) {
    currentUserMode = mode;
    currentUser = (mode == UserMode.store) ? defaultStore : defaultCustomer;
    notifyListeners();
  }

  void toggleTheme() {
    isDarkMode = !isDarkMode;
    notifyListeners();
  }

  void toggleLike(String postId) {
    final index = posts.indexWhere((p) => p.id == postId);
    if (index != -1) {
      final post = posts[index];
      final isLiked = !post.isLiked;
      posts[index] = post.copyWith(
        isLiked: isLiked,
        likesCount: isLiked ? post.likesCount + 1 : post.likesCount - 1,
      );
      notifyListeners();
    }
  }

  void toggleSave(String postId) {
    final index = posts.indexWhere((p) => p.id == postId);
    if (index != -1) {
      final post = posts[index];
      final isSaved = !post.isSaved;
      posts[index] = post.copyWith(
        isSaved: isSaved,
        savesCount: isSaved ? post.savesCount + 1 : post.savesCount - 1,
      );
      notifyListeners();
    }
  }

  void addPost(Post newPost) {
    posts.insert(0, newPost);
    notifyListeners();
  }

  void depositWallet(int amount) {
    walletBalance += amount;
    rewardCoins += (amount / 100000).toInt() * 10;
    notifyListeners();
  }

  bool payFromWallet(int amount) {
    if (walletBalance >= amount) {
      walletBalance -= amount;
      notifyListeners();
      return true;
    }
    return false;
  }

  void payInvoice(String invoiceId) {
    final index = chatMessages.indexWhere((m) => m.invoice?.id == invoiceId);
    if (index != -1) {
      final msg = chatMessages[index];
      if (msg.invoice != null) {
        final amount = msg.invoice!.amount;
        if (walletBalance >= amount) {
          walletBalance -= amount;
          chatMessages[index] = ChatMessage(
            id: msg.id,
            senderId: msg.senderId,
            text: msg.text,
            timestamp: msg.timestamp,
            isFromMe: msg.isFromMe,
            invoice: msg.invoice!.copyWith(isPaid: true, paidAt: 'همین الان'),
          );

          // Add to orders
          orders.insert(
            0,
            MarketOrder(
              id: 'ORD-${DateTime.now().millisecondsSinceEpoch % 10000}',
              productTitle: msg.invoice!.title,
              productImage: 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=800&q=80',
              price: amount,
              status: OrderStatus.preparing,
              orderDate: 'امروز',
              customerName: currentUser.name,
              customerAddress: currentUser.city,
              trackingCode: 'در حال صدور',
            ),
          );

          notifyListeners();
        }
      }
    }
  }

  void sendChatMessage(String text, {Invoice? invoice}) {
    chatMessages.add(
      ChatMessage(
        id: 'msg_${DateTime.now().millisecondsSinceEpoch}',
        senderId: currentUser.id,
        text: text,
        timestamp: 'همین الان',
        isFromMe: true,
        invoice: invoice,
      ),
    );
    notifyListeners();
  }

  void addTryOnBooking(TryOnBooking booking) {
    tryOnBookings.insert(0, booking);
    notifyListeners();
  }
}
