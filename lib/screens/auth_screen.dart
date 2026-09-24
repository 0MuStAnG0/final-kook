import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../state/app_state.dart';

class AuthScreen extends StatefulWidget {
  const AuthScreen({Key? key}) : super(key: key);

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  bool isLogin = true;
  final _phoneController = TextEditingController();
  final _passwordController = TextEditingController();
  final _nameController = TextEditingController();

  @override
  void dispose() {
    _phoneController.dispose();
    _passwordController.dispose();
    _nameController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);

    return Scaffold(
      appBar: AppBar(
        title: Text(isLogin ? 'ورود به کوک' : 'ثبت نام در کوک'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const SizedBox(height: 20),
            Center(
              child: Container(
                width: 72,
                height: 72,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [Color(0xFF4F46E5), Color(0xFF9333EA)],
                  ),
                  borderRadius: BorderRadius.circular(20),
                ),
                alignment: Alignment.Center,
                child: const Text('ک', style: TextStyle(fontSize: 36, fontWeight: FontWeight.bold, color: Colors.white)),
              ),
            ),
            const SizedBox(height: 28),

            if (!isLogin) ...[
              TextField(
                controller: _nameController,
                decoration: InputDecoration(
                  labelText: 'نام و نام خانوادگی',
                  filled: true,
                  fillColor: const Color(0xFF1E293B),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                ),
              ),
              const SizedBox(height: 12),
            ],

            TextField(
              controller: _phoneController,
              keyboardType: TextInputType.phone,
              decoration: InputDecoration(
                labelText: 'شماره موبایل یا ایمیل',
                filled: true,
                fillColor: const Color(0xFF1E293B),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
              ),
            ),
            const SizedBox(height: 12),

            TextField(
              controller: _passwordController,
              obscureText: true,
              decoration: InputDecoration(
                labelText: 'رمز عبور',
                filled: true,
                fillColor: const Color(0xFF1E293B),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
              ),
            ),

            const SizedBox(height: 24),

            ElevatedButton(
              onPressed: () {
                final phone = _phoneController.text.trim();
                if (phone.isEmpty) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('لطفاً شماره موبایل را وارد نمایید')),
                  );
                  return;
                }

                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('ورود با موفقیت انجام شد ✅'), backgroundColor: Colors.green),
                );
                Navigator.pop(context);
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4F46E5),
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
              ),
              child: Text(
                isLogin ? 'ورود به حساب کاربری' : 'تکمیل ثبت نام',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.white),
              ),
            ),

            const SizedBox(height: 16),

            TextButton(
              onPressed: () => setState(() => isLogin = !isLogin),
              child: Text(
                isLogin ? 'حساب کاربری ندارید؟ ثبت‌نام کنید' : 'قبلاً حساب کاربری داشته‌اید؟ وارد شوید',
                style: const TextStyle(color: Color(0xFF4F46E5)),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
