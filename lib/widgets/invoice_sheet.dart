import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/models.dart';
import '../state/app_state.dart';

class CreateInvoiceSheet extends StatefulWidget {
  const CreateInvoiceSheet({Key? key}) : super(key: key);

  static void show(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const CreateInvoiceSheet(),
    );
  }

  @override
  State<CreateInvoiceSheet> createState() => _CreateInvoiceSheetState();
}

class _CreateInvoiceSheetState extends State<CreateInvoiceSheet> {
  final _titleController = TextEditingController();
  final _amountController = TextEditingController();
  final _descController = TextEditingController();

  @override
  void dispose() {
    _titleController.dispose();
    _amountController.dispose();
    _descController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final state = Provider.of<AppStateManager>(context, listen: false);

    return Container(
      padding: EdgeInsets.only(
        top: 20,
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
          Row(
            children: const [
              Icon(Icons.receipt_long, color: Color(0xFF4F46E5)),
              SizedBox(width: 8),
              Text(
                'صدور فاکتور پرداخت آنی',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
              ),
            ],
          ),
          const SizedBox(height: 16),
          TextField(
            controller: _titleController,
            decoration: InputDecoration(
              labelText: 'عنوان محصول یا خدمت',
              filled: true,
              fillColor: const Color(0xFF1E293B),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _amountController,
            keyboardType: TextInputType.number,
            decoration: InputDecoration(
              labelText: 'مبلغ به تومان',
              filled: true,
              fillColor: const Color(0xFF1E293B),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _descController,
            decoration: InputDecoration(
              labelText: 'توضیحات و شرایط ارسال',
              filled: true,
              fillColor: const Color(0xFF1E293B),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
            ),
          ),
          const SizedBox(height: 20),
          ElevatedButton(
            onPressed: () {
              final title = _titleController.text.trim();
              final amount = int.tryParse(_amountController.text.replaceAll(RegExp(r'[^0-9]'), '')) ?? 0;
              final desc = _descController.text.trim();

              if (title.isNotEmpty && amount > 0) {
                final invoice = Invoice(
                  id: 'INV-${DateTime.now().millisecondsSinceEpoch % 10000}',
                  title: title,
                  amount: amount,
                  description: desc.isEmpty ? 'ارسال سریع با پست پیشتاز' : desc,
                );
                state.sendChatMessage('فاکتور جدید برای پرداخت صادر شد:', invoice: invoice);
                Navigator.pop(context);
              }
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF4F46E5),
              padding: const EdgeInsets.symmetric(vertical: 14),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
            ),
            child: const Text('ارسال فاکتور به مشتری', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }
}
