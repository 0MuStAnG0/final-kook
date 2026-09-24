import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/models.dart';
import '../state/app_state.dart';
import '../utils/persian_utils.dart';

class MapScreen extends StatefulWidget {
  const MapScreen({Key? key}) : super(key: key);

  @override
  State<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
  int selectedStoreIndex = 0;

  final List<Map<String, dynamic>> stores = [
    {
      'name': 'بوتیک کوک استایل (شعبه کاج)',
      'category': 'پوشاک و مد بانوان',
      'address': 'تهران، سعادت‌آباد، میدان کاج، پاساژ سروستان',
      'distance': '۱.۲ کیلومتر',
      'hours': '۱۰:۰۰ الی ۲۲:۳۰',
      'avatar': 'https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?auto=format&fit=crop&w=400&q=80',
      'rating': 4.9,
    },
    {
      'name': 'چرم دست‌دوز کهن تبریز',
      'category': 'کیف، کفش و اکسسوری چرم',
      'address': 'تهران، شهرک غرب، بلوار فرحزادی، مرکز خرید میلاد نور',
      'distance': '۲.۸ کیلومتر',
      'hours': '۱۰:۳۰ الی ۲۳:۰۰',
      'avatar': 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80',
      'rating': 4.8,
    },
    {
      'name': 'اسپرت پلاس ایران',
      'category': 'کفش و پوشاک ورزشی اورجینال',
      'address': 'تهران، ونک، خیابان ملاصدرا، پلاک ۷۶',
      'distance': '۴.۵ کیلومتر',
      'hours': '۰۹:۳۰ الی ۲۱:۳۰',
      'avatar': 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80',
      'rating': 4.7,
    },
  ];

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final activeStore = stores[selectedStoreIndex];

    return Scaffold(
      appBar: AppBar(
        title: const Text('نقشه فروشگاه‌های کوک'),
        actions: [
          IconButton(
            icon: const Icon(Icons.my_location),
            onPressed: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('موقعیت مکانی شما در تهران به‌روزرسانی شد')),
              );
            },
          ),
        ],
      ),
      body: Stack(
        children: [
          // Stylized Map Canvas Background
          Container(
            color: const Color(0xFF0F172A),
            child: CustomPaint(
              size: Size.infinite,
              painter: _MapGridPainter(),
            ),
          ),

          // Map Pins Representation
          Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    _buildMapPin(0, 'بوتیک کوک'),
                    const SizedBox(width: 80),
                    _buildMapPin(1, 'چرم کهن'),
                  ],
                ),
                const SizedBox(height: 70),
                _buildMapPin(2, 'اسپرت پلاس'),
              ],
            ),
          ),

          // Top Info Banner
          Positioned(
            top: 16,
            left: 20,
            right: 20,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
              decoration: BoxDecoration(
                color: const Color(0xFF1E293B).withOpacity(0.9),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: const Color(0xFF4F46E5).withOpacity(0.4)),
              ),
              child: Row(
                children: const [
                  Icon(Icons.storefront, color: Color(0xFF4F46E5), size: 20),
                  SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      'امکان مراجعه و پرو حضوری در فروشگاه‌های معتبر کوک',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Bottom Store Card
          Positioned(
            left: 16,
            right: 16,
            bottom: 24,
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF1E293B),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: const Color(0xFF334155)),
                boxShadow: const [
                  BoxShadow(color: Colors.black45, blurRadius: 15, offset: Offset(0, 5)),
                ],
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      CircleAvatar(
                        radius: 24,
                        backgroundImage: NetworkImage(activeStore['avatar']),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              activeStore['name'],
                              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              '${activeStore['category']} • فاصله: ${PersianUtils.toPersianDigits(activeStore['distance'])}',
                              style: TextStyle(color: Colors.grey[400], fontSize: 12),
                            ),
                          ],
                        ),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: Colors.amber.withOpacity(0.2),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Row(
                          children: [
                            const Icon(Icons.star, color: Colors.amber, size: 14),
                            const SizedBox(width: 4),
                            Text(
                              PersianUtils.toPersianDigits(activeStore['rating']),
                              style: const TextStyle(color: Colors.amber, fontWeight: FontWeight.bold, fontSize: 12),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      const Icon(Icons.location_on_outlined, color: Colors.grey, size: 16),
                      const SizedBox(width: 6),
                      Expanded(
                        child: Text(
                          activeStore['address'],
                          style: TextStyle(color: Colors.grey[300], fontSize: 12),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      Expanded(
                        child: ElevatedButton.icon(
                          onPressed: () {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('مسیریابی با نشان یا بلد آغاز شد')),
                            );
                          },
                          icon: const Icon(Icons.directions, size: 18),
                          label: const Text('مسیریابی'),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF4F46E5),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      OutlinedButton.icon(
                        onPressed: () {
                          Navigator.pushNamed(context, '/inbox');
                        },
                        icon: const Icon(Icons.chat_bubble_outline, size: 18),
                        label: const Text('گفتگو'),
                        style: OutlinedButton.styleFrom(
                          foregroundColor: Colors.white,
                          side: const BorderSide(color: Color(0xFF334155)),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMapPin(int index, String title) {
    final isSelected = selectedStoreIndex == index;
    return GestureDetector(
      onTap: () => setState(() => selectedStoreIndex = index),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            decoration: BoxDecoration(
              color: isSelected ? const Color(0xFF4F46E5) : const Color(0xFF1E293B),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: Colors.white24),
            ),
            child: Text(
              title,
              style: TextStyle(
                color: Colors.white,
                fontSize: 11,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
              ),
            ),
          ),
          Icon(
            Icons.location_on,
            color: isSelected ? const Color(0xFF4F46E5) : Colors.redAccent,
            size: isSelected ? 42 : 32,
          ),
        ],
      ),
    );
  }
}

class _MapGridPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = const Color(0xFF1E293B).withOpacity(0.5)
      ..strokeWidth = 1;

    for (double i = 0; i < size.width; i += 40) {
      canvas.drawLine(Offset(i, 0), Offset(i, size.height), paint);
    }
    for (double j = 0; j < size.height; j += 40) {
      canvas.drawLine(Offset(0, j), Offset(size.width, j), paint);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
