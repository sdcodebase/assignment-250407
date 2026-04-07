package com.sdcodebase.assignment.chat.application.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * 시연 / 개발용 기본 provider. 실제 AI 호출 없이 이커머스 고객센터 시나리오에 맞는
 * 정적 응답을 반환한다. 질문 키워드를 분석해 배송·주문·반품·상품 카테고리별로 분기한다.
 */
@Component
@Primary
public class StubAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(StubAiProvider.class);
    private static final String NAME = "stub";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public AiCompletion complete(List<AiMessage> messages, String model) {
        log.info("[{}] complete called: messages={}, model={}", NAME, messages.size(), model);
        String question = lastUserMessage(messages);
        return new AiCompletion(buildResponse(question), model);
    }

    @Override
    public void stream(List<AiMessage> messages, String model, Consumer<String> onChunk) {
        log.info("[{}] stream called: messages={}, model={}", NAME, messages.size(), model);
        String question = lastUserMessage(messages);
        String response = buildResponse(question);
        for (String line : response.split("(?<=\n)")) {
            onChunk.accept(line);
        }
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private String lastUserMessage(List<AiMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).role() == AiMessage.Role.USER) {
                return messages.get(i).content().toLowerCase();
            }
        }
        return "";
    }

    private String buildResponse(String q) {
        if (containsAny(q, "배송", "택배", "도착", "운송장", "배달")) {
            return DELIVERY;
        }
        if (containsAny(q, "주문", "결제", "구매", "영수증", "구입")) {
            return ORDER;
        }
        if (containsAny(q, "반품", "교환", "환불", "취소", "돌려")) {
            return RETURN;
        }
        if (containsAny(q, "상품", "제품", "재고", "가격", "할인", "스펙", "사양")) {
            return PRODUCT;
        }
        if (containsAny(q, "안녕", "hello", "hi", "도움", "뭐", "무엇")) {
            return GREETING;
        }
        return DEFAULT;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    // ─── static responses ─────────────────────────────────────────────────────

    private static final String GREETING =
            "안녕하세요! 스마트몰 AI 고객센터입니다. 😊\n\n" +
            "저는 쇼핑과 관련된 모든 문의를 도와드리는 AI 상담원입니다.\n" +
            "아래 항목에 대해 바로 안내해 드릴 수 있어요:\n\n" +
            "  📦 배송 조회\n" +
            "  📋 주문 확인\n" +
            "  🔄 반품 / 교환 안내\n" +
            "  🛍️ 상품 문의\n\n" +
            "무엇을 도와드릴까요?";

    private static final String DELIVERY =
            "고객님의 배송 현황을 확인했습니다. 📦\n\n" +
            "  주문번호: #SM-2024-8847\n" +
            "  상품명: 프리미엄 무선 블루투스 이어폰 (화이트)\n" +
            "  배송사: CJ대한통운  |  운송장: 631-1234-5678\n" +
            "  현재 상태: 🚚 수도권 물류센터 출고 완료\n" +
            "  예상 도착: 오늘 오후 6시 ~ 내일 오전 중\n\n" +
            "배송 관련 추가 문의는 고객센터(1588-0000)로 연락 주시거나\n" +
            "다시 말씀해 주세요!";

    private static final String ORDER =
            "고객님의 최근 주문 내역을 안내드립니다. 📋\n\n" +
            "  주문번호: #SM-2024-8847\n" +
            "  상품명: 프리미엄 무선 블루투스 이어폰 (화이트)\n" +
            "  수량: 1개\n" +
            "  결제금액: 89,000원\n" +
            "  결제수단: 카카오페이\n" +
            "  주문일시: 2026년 4월 6일 오후 2:34\n" +
            "  현재 상태: 🚚 배송 중\n\n" +
            "주문 관련 추가 문의가 있으시면 편하게 말씀해 주세요!";

    private static final String RETURN =
            "반품 및 교환 절차를 안내드립니다. 🔄\n\n" +
            "[ 신청 가능 기간 ]\n" +
            "  • 반품(단순 변심): 수령 후 7일 이내\n" +
            "  • 교환(상품 불량·오배송): 수령 후 30일 이내\n\n" +
            "[ 배송비 안내 ]\n" +
            "  • 단순 변심: 왕복 배송비 6,000원 고객 부담\n" +
            "  • 불량·오배송: 배송비 전액 스마트몰 부담\n\n" +
            "[ 신청 방법 ]\n" +
            "  1. 마이페이지 → 주문 내역 → 반품/교환 신청\n" +
            "  2. 고객센터 채팅 또는 전화 (1588-0000)\n\n" +
            "추가 문의가 있으시면 언제든지 말씀해 주세요! 😊";

    private static final String PRODUCT =
            "문의하신 상품 정보를 안내드립니다. 🛍️\n\n" +
            "  상품명: 프리미엄 무선 블루투스 이어폰\n" +
            "  색상: 화이트 / 블랙\n" +
            "  정가: 129,000원  →  현재가: 89,000원 (31% 할인)\n" +
            "  재고: ✅ 충분한 재고 보유 중\n" +
            "  배송: 오늘 오후 2시 이전 주문 시 익일 배송\n\n" +
            "[ 주요 스펙 ]\n" +
            "  • 연속 재생: 최대 30시간 (케이스 포함)\n" +
            "  • 노이즈 캔슬링: 지원\n" +
            "  • 방수 등급: IPX5\n\n" +
            "추가로 궁금한 사항이 있으시면 말씀해 주세요! 😊";

    private static final String DEFAULT =
            "안녕하세요! 스마트몰 AI 고객센터입니다. 😊\n\n" +
            "말씀해 주신 내용을 잘 이해하지 못했습니다.\n" +
            "좀 더 구체적으로 질문해 주시거나, 아래 항목 중 선택해 주세요:\n\n" +
            "  📦 배송 조회\n" +
            "  📋 주문 확인\n" +
            "  🔄 반품 / 교환 안내\n" +
            "  🛍️ 상품 문의\n\n" +
            "불편을 드려 죄송합니다. 더 잘 도와드리겠습니다!";
}
