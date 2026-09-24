import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/models.dart';
import '../state/app_state.dart';
import '../utils/persian_utils.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({Key? key}) : super(key: key);

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);
    final user = state.currentUser;
    final isStore = state.currentUserMode == UserMode.store;

    return Scaffold(
      appBar: AppBar(
        title: Text(isStore ? 'پنل مدیریت فروشگاه' : 'پروفایل کاربری'),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            onPressed: () => Navigator.pushNamed(context, '/settings'),
          ),
        ],
      ),
      body: NestedScrollView(
        headerSliverBuilder: (context, innerBoxIsScrolled) => [
          SliverToBoxAdapter(
            child: Column(
              children: [
                // Mode Switcher Banner (Customer vs Store)
                Container(
                  margin: const EdgeInsets.all(16),
                  padding: const EdgeInsets.all(4),
                  decoration: BoxDecoration(
                    color: const Color(0xFF1E293B),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Row(
                    children: [
                      Expanded(
                        child: InkWell(
                          onTap: () => state.switchUserMode(UserMode.customer),
                          borderRadius: BorderRadius.circular(12),
                          child: Container(
                            padding: const EdgeInsets.symmetric(vertical: 10),
                            decoration: BoxDecoration(
                              color: !isStore ? const Color(0xFF4F46E5) : Colors.transparent,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            alignment: Alignment.Center,
                            child: Text(
                              'حالت خریدار 🛍️',
                              style: TextStyle(
                                color: !isStore ? Colors.white : Colors.grey,
                                fontWeight: FontWeight.bold,
                                fontSize: 13,
                              ),
                            ),
                          ),
                        ),
                      ),
                      Expanded(
                        child: InkWell(
                          onTap: () => state.switchUserMode(UserMode.store),
                          borderRadius: BorderRadius.circular(12),
                          child: Container(
                            padding: const EdgeInsets.symmetric(vertical: 10),
                            decoration: BoxDecoration(
                              color: isStore ? const Color(0xFF9333EA) : Colors.transparent,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            alignment: Alignment.Center,
                            child: Text(
                              'حالت فروشنده 🏪',
                              style: TextStyle(
                                color: isStore ? Colors.white : Colors.grey,
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

                // Avatar, Name, Verified Badge
                Center(
                  child: Stack(
                    children: [
                      CircleAvatar(
                        radius: 46,
                        backgroundImage: NetworkImage(user.avatarUrl),
                      ),
                      if (user.isVerified)
                        Positioned(
                          bottom: 0,
                          right: 0,
                          child: Container(
                            padding: const EdgeInsets.all(2),
                            decoration: const BoxDecoration(
                              color: Colors.white,
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(Icons.verified, color: Colors.blueAccent, size: 22),
                          ),
                        ),
                    ],
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  user.name,
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
                ),
                const SizedBox(height: 4),
                Text(
                  '@${user.username} • ${user.city}',
                  style: TextStyle(color: Colors.grey[400], fontSize: 13),
                ),
                if (user.bio != null) ...[
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 8),
                    child: Text(
                      user.bio!,
                      textAlign: TextAlign.center,
                      style: TextStyle(color: Colors.grey[300], fontSize: 12),
                    ),
                  ),
                ],

                // Follower & Rating Stats
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      _buildStatColumn('دنبال‌کننده', PersianUtils.formatCompactNumber(user.followersCount)),
                      Container(width: 1, height: 24, color: Colors.grey[700], margin: const EdgeInsets.symmetric(horizontal: 24)),
                      _buildStatColumn('دنبال‌شونده', PersianUtils.formatCompactNumber(user.followingCount)),
                      Container(width: 1, height: 24, color: Colors.grey[700], margin: const EdgeInsets.symmetric(horizontal: 24)),
                      _buildStatColumn('امتیاز', '${PersianUtils.toPersianDigits(user.rating)} ★'),
                    ],
                  ),
                ),

                // Store verification CTA for regular users
                if (!isStore && user.storeApplication == null) ...[
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                    child: OutlinedButton.icon(
                      onPressed: () => Navigator.pushNamed(context, '/store_apply'),
                      icon: const Icon(Icons.storefront, color: Color(0xFF4F46E5)),
                      label: const Text('ثبت درخواست فروشگاه و کسب درآمد در کوک'),
                      style: OutlinedButton.styleFrom(
                        side: const BorderSide(color: Color(0xFF4F46E5)),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        padding: const EdgeInsets.symmetric(vertical: 12),
                      ),
                    ),
                  ),
                ],

                // Tabs: 1: پست‌ها/محصولات, 2: سفارشات, 3: رزروهای پرو حضوری
                TabBar(
                  controller: _tabController,
                  indicatorColor: const Color(0xFF4F46E5),
                  labelColor: const Color(0xFF4F46E5),
                  unselectedLabelColor: Colors.grey,
                  tabs: [
                    Tab(text: isStore ? 'محصولات من' : 'پست‌ها'),
                    const Tab(text: 'سفارشات من'),
                    const Tab(text: 'رزرو پرو حضوری'),
                  ],
                ),
              ],
            ),
          ),
        ],
        body: TabBarView(
          controller: _tabController,
          children: [
            // Tab 1: Posts
            GridView.builder(
              padding: const EdgeInsets.all(12),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 3,
                crossAxisSpacing: 6,
                mainAxisSpacing: 6,
              ),
              itemCount: state.posts.length,
              itemBuilder: (context, index) {
                return ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: Image.network(
                    state.posts[index].imageUrl,
                    fit: BoxFit.cover,
                  ),
                );
              },
            ),

            // Tab 2: Orders
            ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: state.orders.length,
              itemBuilder: (context, index) {
                final order = state.orders[index];
                return Container(
                  margin: const EdgeInsets.only(bottom: 12),
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: const Color(0xFF1E293B),
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(color: const Color(0xFF334155)),
                  ),
                  child: Row(
                    children: [
                      ClipRRect(
                        borderRadius: BorderRadius.circular(12),
                        child: Image.network(
                          order.productImage,
                          width: 60,
                          height: 60,
                          fit: BoxFit.cover,
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(order.productTitle, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                            const SizedBox(height: 4),
                            Text(PersianUtils.formatToman(order.price), style: const TextStyle(color: Color(0xFF4F46E5), fontWeight: FontWeight.bold)),
                            const SizedBox(height: 4),
                            Text(
                              order.status.title,
                              style: TextStyle(color: Color(order.status.colorHex), fontSize: 11, fontWeight: FontWeight.bold),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),

            // Tab 3: Try-On Bookings
            ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: state.tryOnBookings.length,
              itemBuilder: (context, index) {
                final booking = state.tryOnBookings[index];
                return Container(
                  margin: const EdgeInsets.only(bottom: 12),
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: const Color(0xFF1E293B),
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(color: const Color(0xFF9333EA).withOpacity(0.5)),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          const Icon(Icons.check_circle, color: Colors.green, size: 18),
                          const SizedBox(width: 6),
                          Text(booking.status, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.green)),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Text('فروشگاه: ${booking.storeName}', style: const TextStyle(fontWeight: FontWeight.bold)),
                      const SizedBox(height: 4),
                      Text('محصول: ${booking.productTitle}', style: TextStyle(color: Colors.grey[300])),
                      const SizedBox(height: 4),
                      Text('زمان حضور: ${booking.scheduledDate}', style: const TextStyle(color: Color(0xFFF59E0B))),
                      const SizedBox(height: 4),
                      Text('آدرس: ${booking.storeAddress}', style: TextStyle(color: Colors.grey[400], fontSize: 12)),
                      const SizedBox(height: 8),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                        decoration: BoxDecoration(
                          color: const Color(0xFF0F172A),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text('کد رزرو نوبت: ${booking.bookingCode}', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                      ),
                    ],
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatColumn(String label, String value) {
    return Column(
      children: [
        Text(value, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
        const SizedBox(height: 2),
        Text(label, style: TextStyle(color: Colors.grey[400], fontSize: 12)),
      ],
    );
  }
}
