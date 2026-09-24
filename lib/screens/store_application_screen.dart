import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/models.dart';
import '../state/app_state.dart';

class StoreApplicationScreen extends StatefulWidget {
  const StoreApplicationScreen({Key? key}) : super(key: key);

  @override
  State<StoreApplicationScreen> createState() => _StoreApplicationScreenState();
}

class _StoreApplicationScreenState extends State<StoreApplicationScreen> {
  final _storeNameController = TextEditingController();
  final _cityController = TextEditingController(text: 'تهران');
  final _addressController = TextEditingController();
  final _nationalIdController = TextEditingController();
  final _docTitleController = TextEditingController(text: 'جواز کسب / اینماد');
  final _descController = TextEditingController();

  String _category = 'پوشاک و مد';

  final List<String> categories = [
    'پوشاک و مد',
    'کالای دیجیتال',
    'وسایل نقلیه',
    'املاک',
    'خانه و آشپزخانه',
    'خدمات',
    'زیبایی و سلامت',
  ];

  @override
  void dispose() {
    _storeNameController.dispose();
    _cityController.dispose();
    _addressController.dispose();
    _nationalIdController.dispose();
    _docTitleController.dispose();
    _descController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('ثبت‌نام و افتتاح فروشگاه رسمی'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF4F46E5).withOpacity(0.15),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: const Color(0xFF4F46E5).withOpacity(0.4)),
              ),
              child: Row(
                children: const [
                  Icon(Icons.verified_user, color: Color(0xFF4F46E5), size: 28),
                  SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'با تایید فروشگاه، نشان اصالت آبی، درگاه پرداخت مستقیم و امکان ثبت پرو حضوری برای شما فعال می‌شود.',
                      style: TextStyle(fontSize: 12, height: 1.4),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            TextField(
              controller: _storeNameController,
              decoration: InputDecoration(
                labelText: 'نام برند یا فروشگاه',
                filled: true,
                fillColor: const Color(0xFF1E293B),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
              ),
            ),
            const SizedBox(height: 12),

            DropdownButtonFormField<String>(
              value: _category,
              decoration: InputDecoration(
                labelText: 'دسته‌بندی فعالیت',
                filled: true,
                fillColor: const Color(0xFF1E293B),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
              ),
              dropdownColor: const Color(0xFF1E293B),
              items: categories
                  .map((c) => DropdownMenuItem(value: c, child: Text(c)))
                  .toList(),
              onChanged: (v) => setState(() => _category = v ?? _category),
            ),
            const SizedBox(height: 12),

            TextField(
              controller: _nationalIdController,
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                labelText: 'کد ملی مدیر / شناسه ملی شرکت',
                filled: true,
                fillColor: const Color(0xFF1E293B),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
              ),
            ),
            const SizedBox(height: 12),

            TextField(
              controller: _cityController,
              decoration: InputDecoration(
                labelText: 'استان / شهر',
                filled: true,
                fillColor: const Color(0xFF1E293B),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
              ),
            ),
            const SizedBox(height: 12),

            TextField(
              controller: _addressController,
              decoration: InputDecoration(
                labelText: 'آدرس دقیق فروشگاه فیزیکی یا انبار',
                filled: true,
                fillColor: const Color(0xFF1E293B),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
              ),
            ),
            const SizedBox(height: 12),

            TextField(
              controller: _descController,
              maxLines: 2,
              decoration: InputDecoration(
                labelText: 'معرفی کوتاه و سابقه فعالیت',
                filled: true,
                fillColor: const Color(0xFF1E293B),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
              ),
            ),
            const SizedBox(height: 24),

            ElevatedButton(
              onPressed: () {
                final storeName = _storeNameController.text.trim();
                if (storeName.isEmpty) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('لطفاً نام فروشگاه را وارد کنید')),
                  );
                  return;
                }

                final app = StoreApplication(
                  storeName: storeName,
                  category: _category,
                  city: _cityController.text.trim(),
                  address: _addressController.text.trim(),
                  nationalId: _nationalIdController.text.trim(),
                  documentTitle: _docTitleController.text.trim(),
                  description: _descController.text.trim(),
                  submittedAt: 'همین الان',
                  status: StoreApplicationStatus.pendingReview,
                );

                state.api.submitStoreApplication(app);

                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('درخواست فروشگاه شما با موفقیت ثبت شد و در حال بررسی است.'),
                    backgroundColor: Colors.green,
                  ),
                );

                Navigator.pop(context);
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4F46E5),
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
              ),
              child: const Text('ارسال مدارک برای تایید هویت', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.white)),
            ),
          ],
        ),
      ),
    );
  }
}
