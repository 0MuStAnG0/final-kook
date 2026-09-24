import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/models.dart';
import '../state/app_state.dart';
import '../utils/persian_utils.dart';
import '../widgets/invoice_sheet.dart';

class ChatScreen extends StatefulWidget {
  const ChatScreen({Key? key}) : super(key: key);

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final TextEditingController _messageController = TextEditingController();

  @override
  void dispose() {
    _messageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);
    final isStore = state.currentUserMode == UserMode.store;

    return Scaffold(
      appBar: AppBar(
        titleSpacing: 0,
        title: Row(
          children: const [
            CircleAvatar(
              radius: 18,
              backgroundImage: NetworkImage(
                'https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&w=400&q=80',
              ),
            ),
            SizedBox(width: 10),
            Text('خانه مد هاکوپیان (Hacoupian)', style: TextStyle(fontSize: 15)),
          ],
        ),
        actions: [
          if (isStore) ...[
            IconButton(
              icon: const Icon(Icons.receipt_long, color: Color(0xFF4F46E5)),
              tooltip: 'صدور فاکتور',
              onPressed: () => CreateInvoiceSheet.show(context),
            ),
          ],
        ],
      ),
      body: Column(
        children: [
          // Messages List
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: state.chatMessages.length,
              itemBuilder: (context, index) {
                final msg = state.chatMessages[index];
                return _buildMessageItem(context, msg, state);
              },
            ),
          ),

          // Message Input Bar
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: const BoxDecoration(
              color: Color(0xFF1E293B),
              border: Border(top: BorderSide(color: Color(0xFF334155))),
            ),
            child: SafeArea(
              child: Row(
                children: [
                  IconButton(
                    icon: const Icon(Icons.add_circle_outline, color: Color(0xFF4F46E5)),
                    onPressed: () {
                      if (isStore) {
                        CreateInvoiceSheet.show(context);
                      } else {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('امکان ارسال تصویر یا استعلام محصول')),
                        );
                      }
                    },
                  ),
                  Expanded(
                    child: TextField(
                      controller: _messageController,
                      decoration: const InputDecoration(
                        hintText: 'پیام خود را بنویسید...',
                        border: InputBorder.none,
                        contentPadding: EdgeInsets.symmetric(horizontal: 12),
                      ),
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.send_rounded, color: Color(0xFF4F46E5)),
                    onPressed: () {
                      final text = _messageController.text.trim();
                      if (text.isNotEmpty) {
                        state.sendChatMessage(text);
                        _messageController.clear();
                      }
                    },
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMessageItem(BuildContext context, ChatMessage msg, AppStateManager state) {
    final isMe = msg.isFromMe;

    return Align(
      alignment: isMe ? Alignment.centerLeft : Alignment.centerRight,
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        constraints: BoxConstraints(
          maxWidth: MediaQuery.of(context).size.width * 0.82,
        ),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: isMe ? const Color(0xFF4F46E5) : const Color(0xFF1E293B),
          borderRadius: BorderRadius.only(
            topLeft: const Radius.circular(16),
            topRight: const Radius.circular(16),
            bottomLeft: Radius.circular(isMe ? 0 : 16),
            bottomRight: Radius.circular(isMe ? 16 : 0),
          ),
          border: Border.all(
            color: isMe ? const Color(0xFF6366F1) : const Color(0xFF334155),
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              msg.text,
              style: const TextStyle(fontSize: 14, color: Colors.white, height: 1.4),
            ),
            if (msg.invoice != null) ...[
              const SizedBox(height: 10),
              _buildInvoiceCard(context, msg.invoice!, state),
            ],
            const SizedBox(height: 4),
            Align(
              alignment: Alignment.bottomRight,
              child: Text(
                msg.timestamp,
                style: TextStyle(
                  fontSize: 10,
                  color: isMe ? Colors.white70 : Colors.grey,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInvoiceCard(BuildContext context, Invoice invoice, AppStateManager state) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFF0F172A),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: invoice.isPaid ? Colors.green.withOpacity(0.5) : const Color(0xFFF59E0B).withOpacity(0.5),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                invoice.isPaid ? Icons.check_circle : Icons.receipt_long,
                color: invoice.isPaid ? Colors.green : const Color(0xFFF59E0B),
                size: 20,
              ),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  invoice.title,
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            invoice.description,
            style: const TextStyle(fontSize: 11, color: Colors.grey),
          ),
          const SizedBox(height: 10),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('مبلغ قابل پرداخت:', style: TextStyle(fontSize: 12, color: Colors.grey)),
              Text(
                '${PersianUtils.formatCurrency(invoice.amount)} تومان',
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                  fontSize: 14,
                  color: Color(0xFF10B981),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          if (invoice.isPaid)
            Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(vertical: 8),
              decoration: BoxDecoration(
                color: Colors.green.withOpacity(0.15),
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Center(
                child: Text(
                  'پرداخت شد ✓',
                  style: TextStyle(color: Colors.green, fontWeight: FontWeight.bold, fontSize: 12),
                ),
              ),
            )
          else
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF10B981),
                  shape: RoundedCornerShape(8),
                ),
                icon: const Icon(Icons.payment, size: 16),
                label: const Text('پرداخت امن از کیف پول', style: TextStyle(fontWeight: FontWeight.bold)),
                onPressed: () {
                  if (state.walletBalance >= invoice.amount) {
                    state.payInvoice(invoice.id);
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('پرداخت فاکتور با موفقیت انجام شد')),
                    );
                  } else {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('موجودی کیف پول کافی نیست. لطفاً کیف پول را شارژ نمایید.')),
                    );
                  }
                },
              ),
            ),
        ],
      ),
    );
  }
}
