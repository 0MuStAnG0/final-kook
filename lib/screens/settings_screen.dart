import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../state/app_state.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('تنظیمات کوک'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Server Handshake Card
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: const Color(0xFF1E293B),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: const Color(0xFF334155)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(
                      state.isServerConnected ? Icons.cloud_done : Icons.cloud_off,
                      color: state.isServerConnected ? Colors.green : Colors.red,
                    ),
                    const SizedBox(width: 8),
                    const Text('وضعیت اتصال به سرور مرکزی کوک', style: TextStyle(fontWeight: FontWeight.bold)),
                  ],
                ),
                const SizedBox(height: 8),
                Text('آدرس سرور: ${state.api.baseUrl}', style: const TextStyle(color: Colors.grey, fontSize: 12)),
                const SizedBox(height: 12),
                ElevatedButton.icon(
                  onPressed: () async {
                    final res = await state.api.testConnection();
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text(res ? 'اتصال به سرور OrangeIT برقرار است ✅' : 'سرور در دسترس است (حالت محلی فعال)'),
                        backgroundColor: res ? Colors.green : Colors.blueGrey,
                      ),
                    );
                  },
                  icon: const Icon(Icons.refresh, size: 18),
                  label: const Text('تست ارتباط هندشیک سرور'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF4F46E5),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 20),

          // Theme & Appearance
          SwitchListTile(
            title: const Text('حالت تاریک (Dark Mode)'),
            value: state.isDarkMode,
            onChanged: (_) => state.toggleTheme(),
            activeColor: const Color(0xFF4F46E5),
          ),

          const Divider(color: Color(0xFF334155)),

          ListTile(
            leading: const Icon(Icons.wallet, color: Color(0xFF4F46E5)),
            title: const Text('کیف پول و مالی'),
            trailing: const Icon(Icons.arrow_forward_ios, size: 16),
            onTap: () => Navigator.pushNamed(context, '/wallet'),
          ),

          ListTile(
            leading: const Icon(Icons.storefront, color: Colors.purpleAccent),
            title: const Text('درخواست افتتاح فروشگاه'),
            trailing: const Icon(Icons.arrow_forward_ios, size: 16),
            onTap: () => Navigator.pushNamed(context, '/store_apply'),
          ),

          ListTile(
            leading: const Icon(Icons.lock_outline, color: Colors.amber),
            title: const Text('ورود یا ایجاد حساب کاربری'),
            trailing: const Icon(Icons.arrow_forward_ios, size: 16),
            onTap: () => Navigator.pushNamed(context, '/auth'),
          ),

          const Divider(color: Color(0xFF334155)),

          ListTile(
            leading: const Icon(Icons.info_outline, color: Colors.grey),
            title: const Text('درباره اپلیکیشن کوک'),
            subtitle: const Text('نسخه ۱.۰.۰ وب و فلاتر PWA'),
          ),
        ],
      ),
    );
  }
}
