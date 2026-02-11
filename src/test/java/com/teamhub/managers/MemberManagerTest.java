package com.teamhub.managers;

import com.teamhub.TestBase;
import com.teamhub.common.AppException;
import com.teamhub.common.ErrorCode;
import com.teamhub.models.BillingPlan;
import com.teamhub.models.Member;
import com.teamhub.repositories.MemberRepository;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
class MemberManagerTest extends TestBase {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private BillingManager billingManager;

    private MemberManager memberManager;

    @BeforeEach
    void setUp() {
        memberManager = new MemberManager(memberRepository, billingManager);
    }

    @Test
    void inviteMember_success(Vertx vertx, VertxTestContext ctx) {
        ScenarioSetup setup = scenario().withFreePlan();
        BillingPlan plan = BillingPlan.fromJson(setup.getBillingPlan());

        when(memberRepository.findByEmail("new@teamhub.com", TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(null));
        when(billingManager.getCurrentPlan(TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(plan));
        when(memberRepository.countByOrganization(TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(2L));
        when(memberRepository.insert(any(JsonObject.class)))
                .thenReturn(Future.succeededFuture("member-123"));

        JsonObject body = new JsonObject()
                .put("email", "new@teamhub.com")
                .put("name", "New Member")
                .put("role", "MEMBER");

        memberManager.inviteMember(body, TEST_ORG_ID, TEST_USER_ID)
                .onComplete(ctx.succeeding(member -> {
                    ctx.verify(() -> {
                        assertNotNull(member);
                        assertEquals("new@teamhub.com", member.getEmail());
                        assertEquals(Member.Role.MEMBER, member.getRole());
                        verify(memberRepository).insert(any(JsonObject.class));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void inviteMember_duplicateEmail(Vertx vertx, VertxTestContext ctx) {
        JsonObject existingMember = createTestMember(randomId(), TEST_ORG_ID, "MEMBER");

        when(memberRepository.findByEmail("existing@teamhub.com", TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(existingMember));

        JsonObject body = new JsonObject()
                .put("email", "existing@teamhub.com")
                .put("name", "Duplicate");

        memberManager.inviteMember(body, TEST_ORG_ID, TEST_USER_ID)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.CONFLICT, ((AppException) err).getErrorCode());
                        assertTrue(err.getMessage().contains("already exists"));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void inviteMember_cannotInviteOwner(Vertx vertx, VertxTestContext ctx) {
        JsonObject body = new JsonObject()
                .put("email", "owner@teamhub.com")
                .put("role", "OWNER");

        memberManager.inviteMember(body, TEST_ORG_ID, TEST_USER_ID)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.FORBIDDEN, ((AppException) err).getErrorCode());
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void inviteMember_limitExceeded(Vertx vertx, VertxTestContext ctx) {
        ScenarioSetup setup = scenario().withBillingPlan("FREE", 5, 3);
        BillingPlan plan = BillingPlan.fromJson(setup.getBillingPlan());

        when(memberRepository.findByEmail("new@teamhub.com", TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(null));
        when(billingManager.getCurrentPlan(TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(plan));
        when(memberRepository.countByOrganization(TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(5L));

        JsonObject body = new JsonObject()
                .put("email", "new@teamhub.com")
                .put("name", "New Member");

        memberManager.inviteMember(body, TEST_ORG_ID, TEST_USER_ID)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.FORBIDDEN, ((AppException) err).getErrorCode());
                        assertTrue(err.getMessage().contains("Member limit reached"));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void updateRole_success(Vertx vertx, VertxTestContext ctx) {
        String targetMemberId = randomId();
        String actingUserId = randomId();

        JsonObject actingMemberDoc = createTestMember(actingUserId, TEST_ORG_ID, "ADMIN");
        JsonObject targetMemberDoc = createTestMember(targetMemberId, TEST_ORG_ID, "MEMBER");
        JsonObject updatedMemberDoc = targetMemberDoc.copy().put("role", "VIEWER");

        when(memberRepository.findById(actingUserId))
                .thenReturn(Future.succeededFuture(actingMemberDoc));
        when(memberRepository.findById(targetMemberId))
                .thenReturn(Future.succeededFuture(targetMemberDoc))
                .thenReturn(Future.succeededFuture(updatedMemberDoc));
        when(memberRepository.update(eq(targetMemberId), any(JsonObject.class)))
                .thenReturn(Future.succeededFuture());

        memberManager.updateRole(targetMemberId, "VIEWER", TEST_ORG_ID, actingUserId)
                .onComplete(ctx.succeeding(member -> {
                    ctx.verify(() -> {
                        assertEquals(Member.Role.VIEWER, member.getRole());
                        verify(memberRepository).update(eq(targetMemberId), any(JsonObject.class));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void updateRole_hierarchyViolation(Vertx vertx, VertxTestContext ctx) {
        String targetMemberId = randomId();
        String actingUserId = randomId();

        JsonObject actingMemberDoc = createTestMember(actingUserId, TEST_ORG_ID, "MEMBER");
        JsonObject targetMemberDoc = createTestMember(targetMemberId, TEST_ORG_ID, "ADMIN");

        when(memberRepository.findById(actingUserId))
                .thenReturn(Future.succeededFuture(actingMemberDoc));
        when(memberRepository.findById(targetMemberId))
                .thenReturn(Future.succeededFuture(targetMemberDoc));

        memberManager.updateRole(targetMemberId, "VIEWER", TEST_ORG_ID, actingUserId)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.FORBIDDEN, ((AppException) err).getErrorCode());
                        assertTrue(err.getMessage().contains("equal or higher role"));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void removeMember_success(Vertx vertx, VertxTestContext ctx) {
        String targetMemberId = randomId();
        String actingUserId = randomId();

        JsonObject actingMemberDoc = createTestMember(actingUserId, TEST_ORG_ID, "ADMIN");
        JsonObject targetMemberDoc = createTestMember(targetMemberId, TEST_ORG_ID, "MEMBER");

        when(memberRepository.findById(actingUserId))
                .thenReturn(Future.succeededFuture(actingMemberDoc));
        when(memberRepository.findById(targetMemberId))
                .thenReturn(Future.succeededFuture(targetMemberDoc));
        when(memberRepository.softDelete(targetMemberId))
                .thenReturn(Future.succeededFuture());

        memberManager.removeMember(targetMemberId, TEST_ORG_ID, actingUserId)
                .onComplete(ctx.succeeding(v -> {
                    ctx.verify(() -> {
                        verify(memberRepository).softDelete(targetMemberId);
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void removeMember_cannotRemoveOwner(Vertx vertx, VertxTestContext ctx) {
        String targetMemberId = randomId();
        String actingUserId = randomId();

        JsonObject actingMemberDoc = createTestMember(actingUserId, TEST_ORG_ID, "ADMIN");
        JsonObject targetMemberDoc = createTestMember(targetMemberId, TEST_ORG_ID, "OWNER");

        when(memberRepository.findById(actingUserId))
                .thenReturn(Future.succeededFuture(actingMemberDoc));
        when(memberRepository.findById(targetMemberId))
                .thenReturn(Future.succeededFuture(targetMemberDoc));

        memberManager.removeMember(targetMemberId, TEST_ORG_ID, actingUserId)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.FORBIDDEN, ((AppException) err).getErrorCode());
                        assertTrue(err.getMessage().contains("Cannot remove the organization owner"));
                    });
                    ctx.completeNow();
                }));
    }
}
