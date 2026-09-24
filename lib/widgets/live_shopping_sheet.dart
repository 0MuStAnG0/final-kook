import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/models.dart';
import '../state/app_state.dart';
import '../utils/persian_utils.dart';

class LiveShoppingSheet extends StatefulWidget {
  final Product product;
  final User seller;

  const LiveShoppingSheet({
    Key? key,
    required this.product,
    required this.seller,
  }) : super(key: key);

  static void show(BuildContext context, Product product, User seller) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => LiveShoppingSheet(product: product, seller: seller),
    );
  }

  @override
  State<LiveShoppingSheet> createState() => _LiveShoppingSheetState();
}

class _LiveShoppingSheetState extends State<LiveShoppingSheet> {
  String selectedSize = 'M (۳۸)';
  String selectedColor = 'طوسی روشن';
  int tabIndex = 0; // 0: خرید آنلاین, 1: رزرو پرو حضوری

  final List<String> sizes = ['S (۳۶)', 'M (۳۸)', 'L (۴۰)', 'XL (۴۲)'];
  final List<String> colors = ['طوسی روشن', 'مشکی زغالی', 'کرم شتری'];

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);
    final theme = Theme.of(context);

    return Container(
      padding: EdgeInsets.only(
        top: 16,
        left: 20,
        right: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
      ),
      decoration: BoxDecoration(
        color: theme.scaffoldBackgroundColor,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        border: Border.all(color: const Color(0xFF334155), width: 1),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Drag handle
          Center(
            child: Container(
              width: 40,
              height: 4,
              margin: const EdgeInsets.only(bottom: 16),
              decoration: BoxDecoration(
                color: Colors.grey.withOpacity(0.4),
                borderRadius: BorderRadius.circular(2),
              ),
            ),
          ),

          // Header: Seller info & verified badge
          Row(
            children: [
              CircleAvatar(
                radius: 20,
                backgroundImage: NetworkImage(widget.seller.avatarUrl),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Text(
                          widget.seller.name,
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                        ),
                        if (widget.seller.isVerified) ...[
                          const SizedBox(width: 4),
                          const Icon(Icons.verified, color: Colors.blueAccent, size: 16),
                        ],
                      ],
                    ),
                    Text(
                      'امتیاز: ${PersianUtils.toPersianDigits(widget.seller.rating)} ★ | ${widget.seller.city}',
                      style: TextStyle(color: theme.colorScheme.onSurface.withOpacity(0.6), fontSize: 12),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: Colors.green.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Text(
                  'موجود در انبار',
                  style: TextStyle(color: Colors.green, fontSize: 11, fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),

          const SizedBox(height: 16),
          const Divider(height: 1, color: Color(0xFF334155)),
          const SizedBox(height: 16),

          // Product Image & Price
          Row(
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: Image.network(
                  widget.product.imageUrl ?? '',
                  width: 80,
                  height: 80,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => Container(
                    width: 80,
                    height: 80,
                    color: Colors.grey.withOpacity(0.2),
                    child: const Icon(Icons.image),
                  ),
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      widget.product.title,
                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 6),
                    Row(
                      children: [
                        Text(
                          PersianUtils.formatToman(widget.product.price),
                          style: const TextStyle(
                            color: Color(0xFF4F46E5),
                            fontWeight: FontWeight.w900,
                            fontSize: 17,
                          ),
                        ),
                        if (widget.product.oldPrice != null) ...[
                          const SizedBox(width: 8),
                          Text(
                            PersianUtils.formatToman(widget.product.oldPrice),
                            style: const TextStyle(
                              decoration: TextDecoration.lineThrough,
                              color: Colors.grey,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),

          const SizedBox(height: 16),

          // Tabs: خرید فوری / پرو حضوری
          Container(
            decoration: BoxDecoration(
              color: const Color(0xFF1E293B),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                Expanded(
                  child: InkWell(
                    onTap: () => setState(() => tabIndex = 0),
                    child: Container(
                      padding: const EdgeInsets.symmetric(vertical: 10),
                      decoration: BoxDecoration(
                        color: tabIndex == 0 ? const Color(0xFF4F46E5) : Colors.transparent,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      alignment: Alignment.Center,
                      child: Text(
                        'خرید آنلاین فوری 🚀',
                        style: TextStyle(
                          color: tabIndex == 0 ? Colors.white : Colors.grey,
                          fontWeight: FontWeight.bold,
                          fontSize: 13,
                        ),
                      ),
                    ),
                  ),
                ),
                Expanded(
                  child: InkWell(
                    onTap: () => setState(() => tabIndex = 1),
                    child: Container(
                      padding: const EdgeInsets.symmetric(vertical: 10),
                      decoration: BoxDecoration(
                        color: tabIndex == 1 ? const Color(0xFF4F46E5) : Colors.transparent,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      alignment: Alignment.Center,
                      child: Text(
                        'رزرو پرو حضوری 👗',
                        style: TextStyle(
                          color: tabIndex == 1 ? Colors.white : Colors.grey,
                          fontWeight: FontWeight.bold,
                          fontSize: 13,
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 16),

          // Size Selection
          const Text('انتخاب سایز:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: sizes.map((s) {
              final isSel = selectedSize == s;
              return ChoiceChip(
                label: Text(s),
                selected: isSel,
                onSelected: (_) => setState(() => selectedSize = s),
                selectedColor: const Color(0xFF4F46E5),
                backgroundColor: const Color(0xFF1E293B),
                labelStyle: TextStyle(
                  color: isSel ? Colors.white : Colors.grey[300],
                  fontWeight: isSel ? FontWeight.bold : FontWeight.normal,
                ),
              );
            }).toList(),
          ),

          const SizedBox(height: 20),

          // Action Button
          if (tabIndex == 0) ...[
            ElevatedButton(
              onPressed: () {
                Navigator.pop(context);
                final success = state.payFromWallet(widget.product.price);
                if (success) {
                  state.orders.insert(
                    0,
                    MarketOrder(
                      id: 'ORD-${DateTime.now().millisecondsSinceEpoch % 10000}',
                      productTitle: widget.product.title,
                      productImage: widget.product.imageUrl ?? '',
                      price: widget.product.price,
                      status: OrderStatus.preparing,
                      orderDate: 'امروز',
                      customerName: state.currentUser.name,
                      customerAddress: 'سعادت‌آباد، خیابان سرو، پلاک ۲۴',
                      trackingCode: 'در حال آماده‌سازی',
                    ),
                  );
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('سفارش شما با موفقیت از موجودی کیف پول پرداخت و ثبت شد 🎉'),
                      backgroundColor: Colors.green,
                    ),
                  );
                } else {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('موجودی کیف پول کافی نیست. لطفاً کیف پول خود را شارژ کنید.'),
                      backgroundColor: Colors.redAccent,
                    ),
                  );
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4F46E5),
                padding: const EdgeInsets.symmetric(vertical: 14),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              ),
              child: Text(
                'پرداخت و ثبت سفارش (${PersianUtils.formatToman(widget.product.price)})',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15, color: Colors.white),
              ),
            ),
          ] else ...[
            ElevatedButton(
              onPressed: () {
                Navigator.pop(context);
                state.addTryOnBooking(
                  TryOnBooking(
                    id: 'TB-${DateTime.now().millisecondsSinceEpoch % 1000}',
                    storeName: widget.seller.name,
                    storeAvatar: widget.seller.avatarUrl,
                    productTitle: widget.product.title,
                    productImage: widget.product.imageUrl ?? '',
                    scheduledDate: 'فردا - ۱۷:۰۰',
                    scheduledTime: '۱۷:۰۰ الی ۱۸:۰۰',
                    bookingCode: 'PRO-${DateTime.now().millisecondsSinceEpoch % 1000}',
                    storeAddress: widget.seller.location?.address ?? 'شعبه مرکزی',
                    status: 'رزرو اتاق پرو تایید شد',
                  ),
                );
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('نوبت پرو حضوری در فروشگاه با موفقیت ثبت شد ✅'),
                    backgroundColor: Colors.green,
                  ),
                );
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF9333EA),
                padding: const EdgeInsets.symmetric(vertical: 14),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              ),
              child: const Text(
                'تایید رزرو اتاق پرو حضوری',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15, color: Colors.white),
              ),
            ),
          ],
        ],
      ),
    );
  }
}
