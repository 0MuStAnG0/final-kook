import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/models.dart';
import '../state/app_state.dart';

class AddScreen extends StatefulWidget {
  const AddScreen({Key? key}) : super(key: key);

  @override
  State<AddScreen> createState() => _AddScreenState();
}

class _AddScreenState extends State<AddScreen> {
  final _titleController = TextEditingController();
  final _descController = TextEditingController();
  final _priceController = TextEditingController();
  String _selectedCategory = 'پوشاک و مد';
  bool _hasProduct = true;

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
    _titleController.dispose();
    _descController.dispose();
    _priceController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('ثبت پست و محصول جدید'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Media picker placeholder
            Container(
              height: 200,
              decoration: BoxDecoration(
                color: const Color(0xFF1E293B),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: const Color(0xFF4F46E5), width: 1.5),
              ),
              child: InkWell(
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('ویدیو یا تصویر با موفقیت انتخاب شد')),
                  );
                },
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: const [
                    Icon(Icons.video_call, size: 54, color: Color(0xFF4F46E5)),
                    SizedBox(height: 12),
                    Text(
                      'انتخاب یا ضبط ویدیوی ریلز / عکس محصول',
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                    ),
                    SizedBox(height: 4),
                    Text(
                      'فرمت‌های MP4، JPG، PNG با کیفیت بالا',
                      style: TextStyle(color: Colors.grey, fontSize: 12),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            TextField(
              controller: _descController,
              maxLines: 3,
              decoration: InputDecoration(
                labelText: 'کپشن و توضیحات پست',
                alignLabelWithHint: true,
                filled: true,
                fillColor: const Color(0xFF1E293B),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(16)),
              ),
            ),

            const SizedBox(height: 16),

            // Toggle has product
            SwitchListTile(
              title: const Text('افزودن امکان خرید آنلاین برای این ویدیو'),
              subtitle: const Text('مشتریان می‌توانند مستقیماً از روی ویدیو سفارش دهند'),
              value: _hasProduct,
              onChanged: (val) => setState(() => _hasProduct = val),
              activeColor: const Color(0xFF4F46E5),
            ),

            if (_hasProduct) ...[
              const SizedBox(height: 12),
              TextField(
                controller: _titleController,
                decoration: InputDecoration(
                  labelText: 'نام کالا یا محصول',
                  filled: true,
                  fillColor: const Color(0xFF1E293B),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(16)),
                ),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: _priceController,
                keyboardType: TextInputType.number,
                decoration: InputDecoration(
                  labelText: 'قیمت به تومان',
                  filled: true,
                  fillColor: const Color(0xFF1E293B),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(16)),
                ),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<String>(
                value: _selectedCategory,
                decoration: InputDecoration(
                  labelText: 'دسته‌بندی',
                  filled: true,
                  fillColor: const Color(0xFF1E293B),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(16)),
                ),
                dropdownColor: const Color(0xFF1E293B),
                items: categories
                    .map((cat) => DropdownMenuItem(value: cat, child: Text(cat)))
                    .toList(),
                onChanged: (val) => setState(() => _selectedCategory = val ?? _selectedCategory),
              ),
            ],

            const SizedBox(height: 28),

            ElevatedButton(
              onPressed: () {
                final desc = _descController.text.trim();
                if (desc.isEmpty) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('لطفاً توضیحات پست را وارد کنید')),
                  );
                  return;
                }

                Product? prod;
                if (_hasProduct) {
                  final title = _titleController.text.trim();
                  final price = int.tryParse(_priceController.text.replaceAll(RegExp(r'[^0-9]'), '')) ?? 0;
                  prod = Product(
                    id: 'prod_${DateTime.now().millisecondsSinceEpoch}',
                    title: title.isEmpty ? 'محصول جدید' : title,
                    price: price > 0 ? price : 1500000,
                    imageUrl: 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=800&q=80',
                    category: _selectedCategory,
                  );
                }

                final newPost = Post(
                  id: 'post_${DateTime.now().millisecondsSinceEpoch}',
                  author: state.currentUser,
                  description: desc,
                  likesCount: 0,
                  commentsCount: 0,
                  sharesCount: 0,
                  imageUrl: 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80',
                  product: prod,
                );

                state.addPost(newPost);

                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('پست شما با موفقیت در فید و بازار منتشر شد 🚀'),
                    backgroundColor: Colors.green,
                  ),
                );

                Navigator.pop(context);
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4F46E5),
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              ),
              child: const Text(
                'انتشار ویدیو و محصول',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.white),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
