import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/models.dart';

class ApiService {
  static const String defaultBaseUrl = 'https://kook.orangeit.site/';
  String baseUrl = defaultBaseUrl;

  String? _accessToken;
  String? _refreshToken;

  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal();

  Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    _accessToken = prefs.getString('access_token');
    _refreshToken = prefs.getString('refresh_token');
    baseUrl = defaultBaseUrl;
  }

  Future<void> saveTokens(String access, String refresh) async {
    _accessToken = access;
    _refreshToken = refresh;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('access_token', access);
    await prefs.setString('refresh_token', refresh);
  }

  Future<void> clearTokens() async {
    _accessToken = null;
    _refreshToken = null;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('access_token');
    await prefs.remove('refresh_token');
  }

  bool get isAuthenticated => _accessToken != null && _accessToken!.isNotEmpty;

  Map<String, String> get _headers => {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    if (_accessToken != null && _accessToken!.isNotEmpty)
      'Authorization': 'Bearer $_accessToken',
  };

  // Test server connectivity / handshake
  Future<bool> testConnection() async {
    try {
      final response = await http
          .get(Uri.parse('${baseUrl}api/health'), headers: _headers)
          .timeout(const Duration(seconds: 4));
      return response.statusCode == 200;
    } catch (_) {
      return false;
    }
  }

  // Auth: Login
  Future<Map<String, dynamic>> login(String identifier, String password) async {
    try {
      final response = await http
          .post(
            Uri.parse('${baseUrl}api/auth/login'),
            headers: _headers,
            body: jsonEncode({'identifier': identifier, 'password': password}),
          )
          .timeout(const Duration(seconds: 6));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        await saveTokens(data['accessToken'] ?? '', data['refreshToken'] ?? '');
        return {'success': true, 'data': data};
      }
      return {'success': false, 'error': 'خطا در ورود به حساب'};
    } catch (_) {
      return {'success': false, 'error': 'عدم برقراری ارتباط با سرور'};
    }
  }

  // Auth: Register
  Future<Map<String, dynamic>> register({
    required String fullName,
    required String phone,
    required String city,
    required String password,
  }) async {
    try {
      final response = await http
          .post(
            Uri.parse('${baseUrl}api/auth/register'),
            headers: _headers,
            body: jsonEncode({
              'fullName': fullName,
              'phone': phone,
              'city': city,
              'password': password,
            }),
          )
          .timeout(const Duration(seconds: 6));

      if (response.statusCode == 200 || response.statusCode == 201) {
        final data = jsonDecode(response.body);
        if (data['accessToken'] != null) {
          await saveTokens(data['accessToken'], data['refreshToken'] ?? '');
        }
        return {'success': true, 'data': data};
      }
      return {'success': false, 'error': 'خطا در ثبت نام'};
    } catch (_) {
      return {'success': false, 'error': 'عدم برقراری ارتباط با سرور'};
    }
  }

  // Auth: Update Profile
  Future<Map<String, dynamic>> updateProfile({
    required String fullName,
    required String username,
    required String bio,
    required String city,
    required String interests,
    required String phone,
    required String gmail,
  }) async {
    try {
      final response = await http
          .put(
            Uri.parse('${baseUrl}api/auth/me'),
            headers: _headers,
            body: jsonEncode({
              'fullName': fullName,
              'username': username,
              'bio': bio,
              'city': city,
              'interests': interests,
              'phone': phone,
              'gmail': gmail,
            }),
          )
          .timeout(const Duration(seconds: 6));
      if (response.statusCode == 200) {
        return {'success': true, 'data': jsonDecode(response.body)};
      }
      return {'success': false, 'error': 'به‌روزرسانی پروفایل ناموفق بود'};
    } catch (_) {
      return {'success': false, 'error': 'عدم دسترسی به سرور'};
    }
  }

  // Store Application
  Future<Map<String, dynamic>> submitStoreApplication(StoreApplication app) async {
    try {
      final response = await http
          .post(
            Uri.parse('${baseUrl}api/store/apply'),
            headers: _headers,
            body: jsonEncode(app.toJson()),
          )
          .timeout(const Duration(seconds: 6));

      if (response.statusCode == 200 || response.statusCode == 201) {
        return {'success': true, 'data': jsonDecode(response.body)};
      }
      return {'success': true, 'data': app.toJson()};
    } catch (_) {
      return {'success': true, 'data': app.toJson()};
    }
  }

  // Posts & Reels Feed
  Future<List<dynamic>> getPosts() async {
    try {
      final response = await http
          .get(Uri.parse('${baseUrl}api/posts'), headers: _headers)
          .timeout(const Duration(seconds: 6));
      if (response.statusCode == 200) {
        return jsonDecode(response.body) as List<dynamic>;
      }
    } catch (_) {}
    return [];
  }

  Future<bool> toggleLikePost(String postId) async {
    try {
      final response = await http
          .post(Uri.parse('${baseUrl}api/posts/$postId/like'), headers: _headers)
          .timeout(const Duration(seconds: 5));
      return response.statusCode == 200;
    } catch (_) {
      return false;
    }
  }

  Future<bool> toggleSavePost(String postId) async {
    try {
      final response = await http
          .post(Uri.parse('${baseUrl}api/posts/$postId/save'), headers: _headers)
          .timeout(const Duration(seconds: 5));
      return response.statusCode == 200;
    } catch (_) {
      return false;
    }
  }

  // Stores & Catalog
  Future<List<dynamic>> getStores() async {
    try {
      final response = await http
          .get(Uri.parse('${baseUrl}api/stores'), headers: _headers)
          .timeout(const Duration(seconds: 6));
      if (response.statusCode == 200) {
        return jsonDecode(response.body) as List<dynamic>;
      }
    } catch (_) {}
    return [];
  }

  Future<bool> toggleFollowStore(String storeId) async {
    try {
      final response = await http
          .post(Uri.parse('${baseUrl}api/stores/$storeId/follow'), headers: _headers)
          .timeout(const Duration(seconds: 5));
      return response.statusCode == 200;
    } catch (_) {
      return false;
    }
  }

  // Orders & Multi-Item Checkout
  Future<Map<String, dynamic>> createOrder(Map<String, dynamic> orderPayload) async {
    try {
      final response = await http
          .post(
            Uri.parse('${baseUrl}api/orders'),
            headers: _headers,
            body: jsonEncode(orderPayload),
          )
          .timeout(const Duration(seconds: 6));
      if (response.statusCode == 200 || response.statusCode == 201) {
        return {'success': true, 'data': jsonDecode(response.body)};
      }
      return {'success': false, 'error': 'ثبت سفارش ناموفق بود'};
    } catch (_) {
      return {'success': false, 'error': 'خطای شبکه در ثبت سفارش'};
    }
  }

  // Chat & Direct Messaging
  Future<List<dynamic>> getChats() async {
    try {
      final response = await http
          .get(Uri.parse('${baseUrl}api/chat'), headers: _headers)
          .timeout(const Duration(seconds: 6));
      if (response.statusCode == 200) {
        return jsonDecode(response.body) as List<dynamic>;
      }
    } catch (_) {}
    return [];
  }

  Future<Map<String, dynamic>?> sendChatMessage(
    String chatId, {
    required String text,
    String type = 'TEXT',
    String? productId,
    String? invoiceId,
    int? amount,
  }) async {
    try {
      final response = await http
          .post(
            Uri.parse('${baseUrl}api/chat/$chatId/messages'),
            headers: _headers,
            body: jsonEncode({
              'text': text,
              'type': type,
              if (productId != null) 'productId': productId,
              if (invoiceId != null) 'invoiceId': invoiceId,
              if (amount != null) 'amount': amount,
            }),
          )
          .timeout(const Duration(seconds: 6));
      if (response.statusCode == 200 || response.statusCode == 201) {
        return jsonDecode(response.body) as Map<String, dynamic>;
      }
    } catch (_) {}
    return null;
  }
}
