// Kook Data Models for Flutter & PWA

enum AccountType {
  regularUser('کاربر عادی (خریدار)'),
  storeOwner('فروشگاه تایید شده (فروشنده)');

  final String title;
  const AccountType(this.title);
}

enum StoreApplicationStatus {
  notSubmitted('ثبت‌نام نشده', 0xFF78909C),
  pendingReview('در حال بررسی مدارک', 0xFFFF9800),
  approved('تایید شده و فعال', 0xFF4CAF50),
  rejected('نیاز به اصلاح مدارک', 0xFFF44336);

  final String title;
  final int colorHex;
  const StoreApplicationStatus(this.title, this.colorHex);
}

class StoreApplication {
  final String id;
  final String storeName;
  final String category;
  final String city;
  final String address;
  final String nationalId;
  final String documentTitle;
  final String? documentUri;
  final String description;
  final String submittedAt;
  final StoreApplicationStatus status;
  final String? adminFeedback;

  StoreApplication({
    this.id = 'app_1',
    required this.storeName,
    required this.category,
    required this.city,
    required this.address,
    required this.nationalId,
    required this.documentTitle,
    this.documentUri,
    required this.description,
    required this.submittedAt,
    this.status = StoreApplicationStatus.pendingReview,
    this.adminFeedback,
  });

  Map<String, dynamic> toJson() => {
    'id': id,
    'storeName': storeName,
    'category': category,
    'city': city,
    'address': address,
    'nationalId': nationalId,
    'documentTitle': documentTitle,
    'documentUri': documentUri,
    'description': description,
    'submittedAt': submittedAt,
    'status': status.name,
    'adminFeedback': adminFeedback,
  };
}

class StoreLocation {
  final double latitude;
  final double longitude;
  final String province;
  final String city;
  final String neighborhood;
  final String address;
  final String? postalCode;

  StoreLocation({
    required this.latitude,
    required this.longitude,
    this.province = 'تهران',
    this.city = 'تهران',
    required this.neighborhood,
    required this.address,
    this.postalCode,
  });
}

class User {
  final String id;
  final String username;
  final String name;
  final String avatarUrl;
  final String? bannerUrl;
  final String? bio;
  final String gmail;
  final String phone;
  final String city;
  final String interests;
  final double rating;
  final int followersCount;
  final int followingCount;
  final bool isVerified;
  final bool isLive;
  final bool isSeller;
  final AccountType accountType;
  final StoreApplication? storeApplication;
  final StoreLocation? location;
  final String storeCategory;
  final String openHours;
  final String? phoneOffice;

  User({
    required this.id,
    required this.username,
    required this.name,
    required this.avatarUrl,
    this.bannerUrl,
    this.bio,
    this.gmail = '',
    this.phone = '',
    this.city = 'تهران',
    this.interests = '',
    this.rating = 0.0,
    this.followersCount = 0,
    this.followingCount = 0,
    this.isVerified = false,
    this.isLive = false,
    this.isSeller = false,
    AccountType? accountType,
    this.storeApplication,
    this.location,
    this.storeCategory = 'پوشاک و مد',
    this.openHours = '۱۰:۰۰ الی ۲۲:۰۰',
    this.phoneOffice,
  }) : accountType = accountType ?? (isSeller ? AccountType.storeOwner : AccountType.regularUser);

  User copyWith({
    String? name,
    String? bio,
    String? phone,
    String? city,
    String? avatarUrl,
    bool? isSeller,
    StoreApplication? storeApplication,
    StoreLocation? location,
  }) {
    return User(
      id: id,
      username: username,
      name: name ?? this.name,
      avatarUrl: avatarUrl ?? this.avatarUrl,
      bannerUrl: bannerUrl,
      bio: bio ?? this.bio,
      gmail: gmail,
      phone: phone ?? this.phone,
      city: city ?? this.city,
      interests: interests,
      rating: rating,
      followersCount: followersCount,
      followingCount: followingCount,
      isVerified: isVerified,
      isLive: isLive,
      isSeller: isSeller ?? this.isSeller,
      accountType: (isSeller ?? this.isSeller) ? AccountType.storeOwner : AccountType.regularUser,
      storeApplication: storeApplication ?? this.storeApplication,
      location: location ?? this.location,
      storeCategory: storeCategory,
      openHours: openHours,
      phoneOffice: phoneOffice,
    );
  }
}

class Product {
  final String id;
  final String title;
  final int price;
  final int? oldPrice;
  final bool inStock;
  final String? imageUrl;
  final String? category;
  final String? description;

  Product({
    required this.id,
    required this.title,
    required this.price,
    this.oldPrice,
    this.inStock = true,
    this.imageUrl,
    this.category,
    this.description,
  });
}

class Post {
  final String id;
  final User author;
  final String description;
  final int likesCount;
  final int commentsCount;
  final int sharesCount;
  final int savesCount;
  final Product? product;
  final bool isLiked;
  final bool isSaved;
  final String audioTrack;
  final String imageUrl;
  final String? videoUrl;

  Post({
    required this.id,
    required this.author,
    required this.description,
    required this.likesCount,
    required this.commentsCount,
    required this.sharesCount,
    this.savesCount = 0,
    this.product,
    this.isLiked = false,
    this.isSaved = false,
    this.audioTrack = 'موزیک ترند کوک',
    required this.imageUrl,
    this.videoUrl,
  });

  Post copyWith({
    bool? isLiked,
    bool? isSaved,
    int? likesCount,
    int? savesCount,
  }) {
    return Post(
      id: id,
      author: author,
      description: description,
      likesCount: likesCount ?? this.likesCount,
      commentsCount: commentsCount,
      sharesCount: sharesCount,
      savesCount: savesCount ?? this.savesCount,
      product: product,
      isLiked: isLiked ?? this.isLiked,
      isSaved: isSaved ?? this.isSaved,
      audioTrack: audioTrack,
      imageUrl: imageUrl,
      videoUrl: videoUrl,
    );
  }
}

enum OrderStatus {
  pending('در انتظار تایید فروشگاه', 0xFFFF9800),
  preparing('در حال آماده‌سازی و بسته‌بندی', 0xFF2196F3),
  shipped('ارسال شده (کد رهگیری پست)', 0xFF9C27B0),
  delivered('تحویل داده شده به مشتری', 0xFF4CAF50),
  canceled('لغو شده', 0xFFE53935);

  final String title;
  final int colorHex;
  const OrderStatus(this.title, this.colorHex);
}

class MarketOrder {
  final String id;
  final String productTitle;
  final String productImage;
  final int price;
  final OrderStatus status;
  final String orderDate;
  final String customerName;
  final String customerAddress;
  final String trackingCode;

  MarketOrder({
    required this.id,
    required this.productTitle,
    required this.productImage,
    required this.price,
    required this.status,
    required this.orderDate,
    required this.customerName,
    required this.customerAddress,
    required this.trackingCode,
  });
}

class TryOnBooking {
  final String id;
  final String storeName;
  final String storeAvatar;
  final String productTitle;
  final String productImage;
  final String scheduledDate;
  final String scheduledTime;
  final String bookingCode;
  final String storeAddress;
  final String status;

  TryOnBooking({
    required this.id,
    required this.storeName,
    required this.storeAvatar,
    required this.productTitle,
    required this.productImage,
    required this.scheduledDate,
    required this.scheduledTime,
    required this.bookingCode,
    required this.storeAddress,
    required this.status,
  });
}

class ChatMessage {
  final String id;
  final String senderId;
  final String text;
  final String timestamp;
  final bool isFromMe;
  final Invoice? invoice;

  ChatMessage({
    required this.id,
    required this.senderId,
    required this.text,
    required this.timestamp,
    required this.isFromMe,
    this.invoice,
  });
}

class Invoice {
  final String id;
  final String title;
  final int amount;
  final String description;
  final bool isPaid;
  final String? paidAt;

  Invoice({
    required this.id,
    required this.title,
    required this.amount,
    required this.description,
    this.isPaid = false,
    this.paidAt,
  });

  Invoice copyWith({bool? isPaid, String? paidAt}) {
    return Invoice(
      id: id,
      title: title,
      amount: amount,
      description: description,
      isPaid: isPaid ?? this.isPaid,
      paidAt: paidAt ?? this.paidAt,
    );
  }
}
