package net.corda.node.services.statemachine

import co.paralleluniverse.fibers.Suspendable
import com.google.common.util.concurrent.Futures
import net.corda.core.crypto.Party
import net.corda.core.flows.FlowLogic
import net.corda.core.getOrThrow
import net.corda.core.utilities.unwrap
import net.corda.testing.node.NodeBasedTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FlowVersioningTest : NodeBasedTest() {
    @Test
    fun `core flows receive platform version of initiator`() {
        val (alice, bob) = Futures.allAsList(
                startNode("Alice", platformVersion = 2),
                startNode("Bob", platformVersion = 3)).getOrThrow()
        bob.installCoreFlow(SendAndReceiveFlow::class, ::SendFlow) // Bob will always send back the initiator's platform version
        val resultFuture = alice.services.startFlow(SendAndReceiveFlow("This is discarded", bob.info.legalIdentity)).resultFuture
        assertThat(resultFuture.getOrThrow()).isEqualTo(2)
    }

    private open class SendAndReceiveFlow(val payload: Any, val otherParty: Party) : FlowLogic<Any>() {
        @Suspendable
        override fun call(): Any = sendAndReceive<Any>(otherParty, payload).unwrap { it }
    }

    private open class SendFlow(val otherParty: Party, val payload: Any) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() = send(otherParty, payload)
    }

}