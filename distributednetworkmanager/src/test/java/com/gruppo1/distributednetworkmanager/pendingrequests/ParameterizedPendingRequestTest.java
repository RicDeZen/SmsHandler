package com.gruppo1.distributednetworkmanager.pendingrequests;

import androidx.annotation.NonNull;

import com.dezen.riccardo.networkmanager.Resource;
import com.dezen.riccardo.networkmanager.StringResource;
import com.dezen.riccardo.smshandler.SMSPeer;
import com.gruppo1.distributednetworkmanager.ActionPropagator;
import com.gruppo1.distributednetworkmanager.BinarySet;
import com.gruppo1.distributednetworkmanager.BitSetUtils;
import com.gruppo1.distributednetworkmanager.KadAction;
import com.gruppo1.distributednetworkmanager.Node;
import com.gruppo1.distributednetworkmanager.NodeDataProvider;
import com.gruppo1.distributednetworkmanager.listeners.FindNodeResultListener;
import com.gruppo1.distributednetworkmanager.listeners.FindValueResultListener;
import com.gruppo1.distributednetworkmanager.listeners.InviteResultListener;
import com.gruppo1.distributednetworkmanager.listeners.PingResultListener;
import com.gruppo1.distributednetworkmanager.listeners.StoreResultListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@RunWith(Parameterized.class)
public class ParameterizedPendingRequestTest {

    private static final String testAddress = "+39892424";
    private static final SMSPeer testPeer = new SMSPeer(testAddress);
    private static final BinarySet testBinarySet = new BinarySet(BitSetUtils.hash(testAddress,128));
    private static final StringResource testResource = new StringResource("test","test");

    private PendingRequest testedRequest;
    private int operationId;
    private int propagatedActions;
    private int expectedActionsOnStart;

    private KadAction exampleValidResponse;
    private KadAction exampleInvalidIdResponse;
    private List<KadAction> exampleInvalidTypeResponses;

    private KadAction buildResponseAction(KadAction.ActionType type, int code){
        switch(type){
            case PING_ANSWER:
                return new KadAction(testPeer,type,code,1,1, KadAction.PayloadType.IGNORED," ");
            case INVITE_ANSWER:
                return new KadAction(testPeer,type,code,1,1, KadAction.PayloadType.BOOLEAN,"true");
            case FIND_NODE_ANSWER:
                return new KadAction(testPeer,type,code,1,1, KadAction.PayloadType.PEER_ADDRESS,testPeer.getAddress());
            case FIND_VALUE_ANSWER:
                return new KadAction(testPeer,type,code,1,1, KadAction.PayloadType.PEER_ADDRESS,testPeer.getAddress());
            case STORE_ANSWER:
                return new KadAction(testPeer,type,code,1,1, KadAction.PayloadType.BOOLEAN,"true");
        }
        return null;
    }

    //Stub node data provider
    NodeDataProvider nodeDataProvider = new NodeDataProvider() {
        @Override
        public Node getClosest(Object target) {
            return null;
        }

        @Override
        public List getKClosest(int k, Object target) {
            return null;
        }

        @Override
        public List filterKClosest(int k, Object target, List nodes) {
            return null;
        }
    };
    //Stub action propagator
    ActionPropagator actionPropagator = new ActionPropagator() {
        @Override
        public void propagateAction(KadAction action) {
            propagatedActions++;
        }
    };
    //Stub result listener
    ResultListener resultListener = new ResultListener();
    private class ResultListener implements PingResultListener, InviteResultListener, FindNodeResultListener, FindValueResultListener, StoreResultListener {
        @Override
        public void onFindNodeResult(int operationId, BitSet target, SMSPeer closest) {

        }

        @Override
        public void onFindValueResult(int operationId, SMSPeer owner, Resource resource) {

        }

        @Override
        public void onInviteResult(int operationId, SMSPeer invited, boolean accepted) {

        }

        @Override
        public void onPingResult(int operationId, SMSPeer pinged, boolean isOnline) {

        }

        @Override
        public void onStoreResult(int operationId, Resource storedResource, SMSPeer newOwner) {

        }
    }

    /**
     * Params for tests
     * @return array of parameters as follows:
     * [PendingRequest class],
     * [Expected response type]
     * [Specific parameter for each class],
     * [Expected number of actions on start],
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {PingPendingRequest.class, KadAction.ActionType.PING_ANSWER, testPeer, 1},
                {InvitePendingRequest.class, KadAction.ActionType.INVITE_ANSWER, testPeer, 1},
                {FindNodePendingRequest.class, KadAction.ActionType.FIND_NODE_ANSWER, testBinarySet, 0},
                {FindValuePendingRequest.class, KadAction.ActionType.FIND_VALUE_ANSWER, testBinarySet, 0},
                {StorePendingRequest.class, KadAction.ActionType.STORE_ANSWER, testResource, 0}
        });
    }

    //Need to be declared separately for switch/series of if in constructor
    private static final String PING_NAME = PingPendingRequest.class.getSimpleName();
    private static final String INVITE_NAME = InvitePendingRequest.class.getSimpleName();
    private static final String FIND_NODE_NAME = FindNodePendingRequest.class.getSimpleName();
    private static final String FIND_VALUE_NAME = FindValuePendingRequest.class.getSimpleName();
    private static final String STORE_NAME = StorePendingRequest.class.getSimpleName();


    /**
     * Constructor for test.
     * @param pendingRequestClass the class of the PendingRequest to test
     * @param param the Only distinctive parameter of the various Requests
     */
    public ParameterizedPendingRequestTest(
            @NonNull Class<PendingRequest> pendingRequestClass,
            @NonNull KadAction.ActionType responseType,
            @NonNull Object param,
            int expectedActionsOnStart
    ){
        try{
            this.expectedActionsOnStart = expectedActionsOnStart;
            propagatedActions = 0;
            operationId = new Random().nextInt();
            Constructor constructor = pendingRequestClass.getConstructors()[0];
            String className = pendingRequestClass.getSimpleName();
            //Switch seems to only work with Strings that are defined through hardcoding. Final attribute was of little help
            if(className.equals(PING_NAME)){
                testedRequest = (PendingRequest) constructor.newInstance(
                        operationId,
                        param,
                        actionPropagator,
                        nodeDataProvider,
                        resultListener
                );
            }
            if(className.equals(INVITE_NAME)){
                testedRequest = (PendingRequest) constructor.newInstance(
                        operationId,
                        param,
                        actionPropagator,
                        nodeDataProvider,
                        resultListener
                );
            }
            if(className.equals(FIND_NODE_NAME)){
                testedRequest = (PendingRequest) constructor.newInstance(
                        operationId,
                        param,
                        actionPropagator,
                        nodeDataProvider,
                        resultListener
                );
            }
            if(className.equals(FIND_VALUE_NAME)){
                testedRequest = (PendingRequest) constructor.newInstance(
                        operationId,
                        param,
                        actionPropagator,
                        nodeDataProvider,
                        resultListener
                );
            }
            if(className.equals(STORE_NAME)){
                testedRequest = (PendingRequest) constructor.newInstance(
                        operationId,
                        param,
                        actionPropagator,
                        nodeDataProvider,
                        resultListener
                );
            }
            generateExampleResponses(responseType);
        }
        catch(IllegalAccessException | InstantiationException | InvocationTargetException e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Method to generate the example Responses from the expected correct type
     * @param responseType the expected Response type
     */
    private void generateExampleResponses(KadAction.ActionType responseType){
        exampleInvalidTypeResponses = new ArrayList<>();
        exampleValidResponse = buildResponseAction(responseType,operationId);
        exampleInvalidIdResponse = buildResponseAction(responseType,operationId+1);
        for(KadAction.ActionType invalidType : KadAction.ActionType.values()){
            if(invalidType != responseType)
                exampleInvalidTypeResponses.add(buildResponseAction(invalidType, operationId));
        }
    }

    @Test
    public void getOperationId_returnsId(){
        assertEquals(operationId, testedRequest.getOperationId());
    }

    @Test
    public void start_propagatesTheAction(){
        testedRequest.start();
        assertEquals(expectedActionsOnStart, propagatedActions);
    }

    @Test
    public void isPertinent_validResponse(){
        assertTrue(testedRequest.isActionPertinent(exampleValidResponse));
    }

    @Test
    public void isPertinent_invalidResponses(){
        assertFalse(testedRequest.isActionPertinent(exampleInvalidIdResponse));
        for(KadAction exampleResponse : exampleInvalidTypeResponses){
            assertFalse(testedRequest.isActionPertinent(exampleResponse));
        }
    }

    /**
     * isPertinent is always executed during nextStep, and {@link PendingRequest#getStepsTaken()}
     */
    @Test
    public void nextStep_acceptsPertinent(){
        final int expectedSteps = 1;
        testedRequest.nextStep(exampleValidResponse);
        assertEquals(expectedSteps, testedRequest.getStepsTaken());
    }

    @Test
    public void nextStep_ignoresNonPertinent(){
        final int expectedSteps = 1;
        testedRequest.nextStep(exampleInvalidIdResponse);
        for(KadAction exampleResponse : exampleInvalidTypeResponses){
            testedRequest.nextStep(exampleResponse);
        }
        assertEquals(expectedSteps, testedRequest.getStepsTaken());
    }
}
