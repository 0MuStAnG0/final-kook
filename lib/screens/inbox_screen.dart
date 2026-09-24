import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../state/app_state.dart';
import '../utils/persian_utils.dart';

class InboxScreen extends StatelessWidget {
  const InboxScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('پیام‌ها و گفتگوها'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(12),
        children: [
          // Chat item with Hacoupian
          InkWell(
            onTap: () => Navigator.pushNamed(context, '/chat'),
            borderRadius: BorderRadius.circular(16),
            child: Container(
              padding: const EdgeInsets.all(12),
              margin: const EdgeInsets.only(bottom: 8),
              decoration: BoxDecoration(
                color: const Color(0xFF1E293B),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: const Color(0xFF334155)),
              ),
              child: Row(
                children: [
                  Stack(
                    children: [
                      const CircleAvatar(
                        radius: 26,
                        backgroundImage: NetworkImage(
                          'https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&w=400&q=80',
                        ),
                      ),
                      Positioned(
                        bottom: 0,
                        right: 0,
                        child: Container(
                          width: 12,
                          height: 12,
                          decoration: BoxDecoration(
                            color: Colors.green,
                            shape: BoxShape.circle,
                            border: Border.all(color: const Color(0xFF1E293B), width: 2),
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: const [
                            Text(
                              'خانه مد هاکوپیان (Hacoupian)',
                              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                            ),
                            Text(
                              '۱۰:۲۸',
                              style: TextStyle(color: Colors.grey, fontSize: 11),
                            ),
                          ],
                        ),
                        const SizedBox(height: 6),
                        Row(
                          children: const [
                            Icon(Icons.receipt, color: Color(0xFF4F46E5), size: 14),
                            SizedBox(width: 4),
                            Expanded(
                              child: Text(
                                'فاکتور کت و شلوار تشریفاتی هاکوپیان صادر شد',
                                style: TextStyle(color: Colors.grey, fontSize: 12),
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Second chat: Mashhad Leather
          Container(
            padding: const EdgeInsets.all(12),
            margin: const EdgeInsets.only(bottom: 8),
            decoration: BoxDecoration(
              color: const Color(0xFF1E293B).withOpacity(0.6),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Row(
              children: [
                const CircleAvatar(
                  radius: 26,
                  backgroundImage: NetworkImage(
                    'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=400&q=80',
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: const [
                          Text('چرم مشهد - واحد پشتیبانی فروش', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                          Text('دیروز', style: TextStyle(color: Colors.grey, fontSize: 11)),
                        ],
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        'سفارش کاپشن چرم طبیعی شما تحویل پست پیشتاز گردید.',
                        style: TextStyle(color: Colors.grey, fontSize: 12),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),

          // Third chat: Novin Leather
          Container(
            padding: const EdgeInsets.all(12),
            margin: const EdgeInsets.only(bottom: 8),
            decoration: BoxDecoration(
              color: const Color(0xFF1E293B).withOpacity(0.6),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Row(
              children: [
                const CircleAvatar(
                  radius: 26,
                  backgroundImage: NetworkImage(
                    'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=400&q=80',
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: const [
                          Text('نوین چرم (Novin Leather)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                          Text('۳ روز پیش', style: TextStyle(color: Colors.grey, fontSize: 11)),
                        ],
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        'کارت گارانتی ۲۴ ماهه طلایی برای کیف اداری شما فعال شد.',
                        style: TextStyle(color: Colors.grey, fontSize: 12),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
