import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../state/app_state.dart';
import '../utils/persian_utils.dart';

class WalletScreen extends StatelessWidget {
  const WalletScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('کیف پول کوک'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Balance Card
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFF4F46E5), Color(0xFF7C3AED)],
                  begin: Alignment.topRight,
                  end: Alignment.bottomLeft,
                ),
                borderRadius: BorderRadius.circular(24),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFF4F46E5).withOpacity(0.3),
                    blurRadius: 20,
                    offset: const Offset(0, 8),
                  ),
                ],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'موجودی قابل استفاده',
                        style: TextStyle(color: Colors.white70, fontSize: 13),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                        decoration: BoxDecoration(
                          color: Colors.white24,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Row(
                          children: [
                            const Icon(Icons.monetization_on, color: Colors.amber, size: 16),
                            const SizedBox(width: 4),
                            Text(
                              '${PersianUtils.toPersianDigits(state.rewardCoins)} سکه',
                              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Text(
                    PersianUtils.formatToman(state.walletBalance),
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 28,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                  const SizedBox(height: 20),
                  Row(
                    children: [
                      Expanded(
                        child: ElevatedButton.icon(
                          onPressed: () => _showDepositDialog(context, state),
                          icon: const Icon(Icons.add, color: Color(0xFF4F46E5)),
                          label: const Text('افزایش موجودی', style: TextStyle(color: Color(0xFF4F46E5), fontWeight: FontWeight.bold)),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(vertical: 12),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: OutlinedButton.icon(
                          onPressed: () {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('درخواست تسویه حساب به شماره شبا ثبت شد')),
                            );
                          },
                          icon: const Icon(Icons.arrow_upward, color: Colors.white),
                          label: const Text('برداشت وجه', style: TextStyle(color: Colors.white)),
                          style: OutlinedButton.styleFrom(
                            side: const BorderSide(color: Colors.white54),
                            padding: const EdgeInsets.symmetric(vertical: 12),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),

            const SizedBox(height: 28),

            // Daily Reward Banner
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF1E293B),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: Colors.amber.withOpacity(0.3)),
              ),
              child: Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: Colors.amber.withOpacity(0.15),
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(Icons.card_giftcard, color: Colors.amber, size: 28),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: const [
                        Text('پاداش حضور روزانه', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                        SizedBox(height: 2),
                        Text('با هر خرید و بازدید از استوری‌ها سکه دریافت کنید', style: TextStyle(color: Colors.grey, fontSize: 12)),
                      ],
                    ),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      state.depositWallet(50000);
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('۵۰,۰۰۰ تومان پاداش دریافت شد 🎁'), backgroundColor: Colors.green),
                      );
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.amber,
                      foregroundColor: Colors.black,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                    child: const Text('دریافت', style: TextStyle(fontWeight: FontWeight.bold)),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 28),

            const Text(
              'تراکنش‌های اخیر',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
            ),
            const SizedBox(height: 12),

            _buildTransactionItem('افزایش موجودی از طریق درگاه پرداخت', '+۱,۰۰۰,۰۰۰ تومان', 'امروز - ۱۰:۴۰', true),
            _buildTransactionItem('خرید آنلاین کت بلیزر اورسایز', '-۱,۸۵۰,۰۰۰ تومان', 'دیروز - ۱۸:۲۰', false),
            _buildTransactionItem('پاداش سکه‌های تخفیف کوک', '+۵۰,۰۰۰ تومان', '۳ روز پیش', true),
          ],
        ),
      ),
    );
  }

  Widget _buildTransactionItem(String title, String amount, String date, bool isPositive) {
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFF1E293B),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0xFF334155)),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Icon(
                isPositive ? Icons.arrow_downward : Icons.arrow_upward,
                color: isPositive ? Colors.green : Colors.redAccent,
                size: 20,
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                  const SizedBox(height: 2),
                  Text(date, style: const TextStyle(color: Colors.grey, fontSize: 11)),
                ],
              ),
            ],
          ),
          Text(
            amount,
            style: TextStyle(
              color: isPositive ? Colors.green : Colors.redAccent,
              fontWeight: FontWeight.bold,
              fontSize: 13,
            ),
          ),
        ],
      ),
    );
  }

  void _showDepositDialog(BuildContext context, AppStateManager state) {
    final controller = TextEditingController(text: '۵۰۰،۰۰۰');
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        backgroundColor: const Color(0xFF131B2E),
        title: const Text('افزایش موجودی کیف پول'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('مبلغ مورد نظر را به تومان وارد کنید:'),
            const SizedBox(height: 12),
            TextField(
              controller: controller,
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                suffixText: 'تومان',
                filled: true,
                fillColor: const Color(0xFF1E293B),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('انصراف'),
          ),
          ElevatedButton(
            onPressed: () {
              state.depositWallet(1000000);
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('کیف پول شما با موفقیت شارژ شد'), backgroundColor: Colors.green),
              );
            },
            style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF4F46E5)),
            child: const Text('اتصال به درگاه و پرداخت', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }
}
