import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/models.dart';
import '../state/app_state.dart';
import '../utils/persian_utils.dart';
import '../widgets/live_shopping_sheet.dart';

class DiscoverScreen extends StatefulWidget {
  const DiscoverScreen({Key? key}) : super(key: key);

  @override
  State<DiscoverScreen> createState() => _DiscoverScreenState();
}

class _DiscoverScreenState extends State<DiscoverScreen> {
  String selectedCategory = 'همه';
  final TextEditingController _searchController = TextEditingController();

  final List<String> categories = [
    'همه',
    'پوشاک و مد',
    'کیف و کفش چرم',
    'کت و شلوار مجلسی',
    'کفش طبی و راحتی',
    'استریت‌استایل و پافر',
    'اکسسوری و کمربند',
  ];

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);
    final theme = Theme.of(context);

    final filteredPosts = state.posts.where((p) {
      if (selectedCategory != 'همه') {
        if (p.product?.category != null && p.product?.category != selectedCategory) {
          return false;
        }
      }
      final query = _searchController.text.trim();
      if (query.isNotEmpty) {
        return p.description.contains(query) || (p.product?.title.contains(query) ?? false);
      }
      return true;
    }).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('کاوش و بازار کوک'),
        actions: [
          IconButton(
            icon: const Icon(Icons.tune),
            onPressed: () => _showFilterDialog(context),
          ),
        ],
      ),
      body: Column(
        children: [
          // Search Bar
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: TextField(
              controller: _searchController,
              onChanged: (_) => setState(() {}),
              decoration: InputDecoration(
                hintText: 'جستجو در میان هزاران کالا و خدمات...',
                prefixIcon: const Icon(Icons.search),
                filled: true,
                fillColor: const Color(0xFF1E293B),
                contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 0),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(16),
                  borderSide: BorderSide.none,
                ),
              ),
            ),
          ),

          // Categories Horizontal List
          SizedBox(
            height: 44,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 12),
              itemCount: categories.length,
              itemBuilder: (context, index) {
                final cat = categories[index];
                final isSelected = selectedCategory == cat;
                return Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 4),
                  child: FilterChip(
                    label: Text(cat),
                    selected: isSelected,
                    onSelected: (_) => setState(() => selectedCategory = cat),
                    selectedColor: const Color(0xFF4F46E5),
                    backgroundColor: const Color(0xFF1E293B),
                    labelStyle: TextStyle(
                      color: isSelected ? Colors.white : Colors.grey[300],
                      fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                    ),
                  ),
                );
              },
            ),
          ),

          const SizedBox(height: 12),

          // Brand Showcase Advertising Banner
          _buildBrandShowcaseBanner(),

          const SizedBox(height: 8),

          // Grid of Products / Reels
          Expanded(
            child: filteredPosts.isEmpty
                ? const Center(child: Text('موردی مطابق با جستجوی شما یافت نشد'))
                : GridView.builder(
                    padding: const EdgeInsets.all(16),
                    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 2,
                      crossAxisSpacing: 12,
                      mainAxisSpacing: 12,
                      childAspectRatio: 0.72,
                    ),
                    itemCount: filteredPosts.length,
                    itemBuilder: (context, index) {
                      final post = filteredPosts[index];
                      return _buildProductCard(context, post);
                    },
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildProductCard(BuildContext context, Post post) {
    return InkWell(
      onTap: () {
        if (post.product != null) {
          LiveShoppingSheet.show(context, post.product!, post.author);
        }
      },
      borderRadius: BorderRadius.circular(16),
      child: Container(
        decoration: BoxDecoration(
          color: const Color(0xFF1E293B),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: const Color(0xFF334155)),
        ),
        clipBehavior: Clip.antiAlias,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Stack(
                fit: StackFit.expand,
                children: [
                  Image.network(
                    post.imageUrl,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => Container(color: Colors.grey[800]),
                  ),
                  Positioned(
                    top: 8,
                    left: 8,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: Colors.black54,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.favorite, color: Colors.redAccent, size: 12),
                          const SizedBox(width: 4),
                          Text(
                            PersianUtils.formatCompactNumber(post.likesCount),
                            style: const TextStyle(color: Colors.white, fontSize: 10),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    post.product?.title ?? post.description,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                  ),
                  const SizedBox(height: 4),
                  if (post.product != null) ...[
                    Text(
                      PersianUtils.formatToman(post.product!.price),
                      style: const TextStyle(
                        color: Color(0xFF4F46E5),
                        fontWeight: FontWeight.w900,
                        fontSize: 13,
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showFilterDialog(BuildContext context) {
    showModalBottomSheet(
      context: context,
      backgroundColor: const Color(0xFF131B2E),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => Container(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text('فیلتر پیشرفته دسته و شهر', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 16),
            ListTile(
              leading: const Icon(Icons.location_on, color: Color(0xFF4F46E5)),
              title: const Text('انتخاب استان / شهر'),
              subtitle: const Text('تهران (همه شهرها)'),
              trailing: const Icon(Icons.arrow_forward_ios, size: 16),
              onTap: () => Navigator.pop(context),
            ),
            ListTile(
              leading: const Icon(Icons.attach_money, color: Colors.amber),
              title: const Text('محدوده قیمت'),
              subtitle: const Text('از ارزان‌ترین به گران‌ترین'),
              trailing: const Icon(Icons.arrow_forward_ios, size: 16),
              onTap: () => Navigator.pop(context),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildBrandShowcaseBanner() {
    final showcaseItems = [
      {
        'brand': 'نوین چرم | Novin Leather',
        'desc': 'جشنواره خرید مستقیم کاپشن، کیف و کفش چرم طبیعی صادراتی',
        'badge': 'تخفیف ۳۵٪',
        'img': 'https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=600&q=80',
        'color': const Color(0xFF831843),
      },
      {
        'brand': 'هاکوپیان | HAKOPIAN',
        'desc': 'هنر اصیل خیاطی، کت و شلوارهای پشمی ایتالیایی و پرو حضوری',
        'badge': '۱۰۰٪ پشم خالص',
        'img': 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=600&q=80',
        'color': const Color(0xFF0F172A),
      },
      {
        'brand': 'چرم درسا | Dorsa Group',
        'desc': 'کلکسیون لوکس کیف‌های مجلسی سنگی و کفش‌های چرم خاص‌پسند',
        'badge': 'کالکشن دیبا',
        'img': 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=600&q=80',
        'color': const Color(0xFF701A75),
      },
      {
        'brand': 'مارال چرم | Maral Leather',
        'desc': 'بوت‌ها و نیم‌بوت‌های زمستانه نبوک و چرم دست‌دوز طبی',
        'badge': 'ضمانت ۲ ساله',
        'img': 'https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=600&q=80',
        'color': const Color(0xFF451A03),
      },
      {
        'brand': 'بادی اسپینر | Body Spinner',
        'desc': 'کاپشن پافر ضدآب، هودی و استریت‌استایل مدرن ایرانی',
        'badge': 'حراج فصل',
        'img': 'https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=600&q=80',
        'color': const Color(0xFF1E3A8A),
      },
    ];

    return SizedBox(
      height: 120,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        itemCount: showcaseItems.length,
        itemBuilder: (context, index) {
          final item = showcaseItems[index];
          return Container(
            width: 290,
            margin: const EdgeInsets.symmetric(horizontal: 4),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(16),
              gradient: LinearGradient(
                colors: [
                  (item['color'] as Color).withOpacity(0.9),
                  const Color(0xFF1E293B).withOpacity(0.95),
                ],
                begin: Alignment.topRight,
                end: Alignment.bottomLeft,
              ),
              border: Border.all(color: Colors.white12),
            ),
            clipBehavior: Clip.antiAlias,
            child: Stack(
              children: [
                Positioned(
                  left: 0,
                  top: 0,
                  bottom: 0,
                  width: 90,
                  child: Opacity(
                    opacity: 0.6,
                    child: Image.network(
                      item['img'] as String,
                      fit: BoxFit.cover,
                      errorBuilder: (_, __, ___) => const SizedBox(),
                    ),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                            decoration: BoxDecoration(
                              color: const Color(0xFF10B981),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Text(
                              item['badge'] as String,
                              style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold),
                            ),
                          ),
                          const Spacer(),
                          const Icon(Icons.verified, color: Color(0xFF38BDF8), size: 14),
                        ],
                      ),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            item['brand'] as String,
                            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Colors.white),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            item['desc'] as String,
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(fontSize: 10, color: Colors.white70),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
