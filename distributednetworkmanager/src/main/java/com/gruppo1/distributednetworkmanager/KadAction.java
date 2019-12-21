package com.gruppo1.distributednetworkmanager;

import androidx.annotation.NonNull;

import com.dezen.riccardo.networkmanager.StringResource;
import com.dezen.riccardo.smshandler.SMSMessage;
import com.dezen.riccardo.smshandler.SMSPeer;
import com.dezen.riccardo.smshandler.exceptions.InvalidMessageException;

import java.util.BitSet;

/***
 * Class defining an Action travelling through a Kademlia Network.
 * Syntax for a Message is as follows:
 * [ACTION TYPE] [OPERATION ID] [PART K] [MAX PARTS] [PAYLOAD TYPE] [PAYLOAD]
 * @author  Pardeep Kumar, Riccardo De Zen
 */

public class KadAction implements DistributedNetworkAction<String, SMSPeer, SMSMessage> {
    private static final int OPERATION_ID_LENGTH=4;
    private static final int CURRENT_PART_LENGTH=4;
    private static final int TOTAL_PARTS_LENGTH=4;
    private static final int ACTION_TYPE_START_INDEX=0;
    private static final int OPERATION_ID_START_INDEX=1;
    private static final int CURRENT_PART_START_INDEX=5;
    private static final int TOTAL_PARTS_START_INDEX=9;
    private static final int PAYLOAD_TYPE_START_INDEX=13;
    private static final int PAYLOAD_START_INDEX=14;
    private static final char PARSING_CHARACTER='0';
    // The length of the Node_ID
    private static final int ID_LENGTH = 128;
    public static final int MIN_ID = 1;
    public static final int MAX_ID = 999;
    public static final String RESOURCE_SEPARATOR="\r";
    public static final int MIN_PARTS=1;
    public static final int MAX_PARTS = 999;
    public static final KadAction INVALID_KAD_ACTION = new KadAction(Token.invalidityToken);
    private final String ACTION_CODE_NOT_FOUND_ERROR_MSG = "Expected ActionType as int number, found not parsable String instead";
    private final String PAYLOAD_TYPE_NOT_FOUND_ERROR_MSG="Expected PayloadType as int number, found not parsable String instead";
    private final String INVALID_ONLY_EXPECTED = "This constructor should be used to build an invalid Kad Action only";

    private SMSPeer actionPeer;
    private ActionType actionType;
    private int operationId;
    private int currentPart;
    private int totalParts;
    private PayloadType payloadType;
    private String payload;



    /**
     * Enum for Action type
     */
    public enum ActionType {
        INVALID(-1),
        //Codes should be even for Requests, odd for Responses
        PING(0),
        PING_ANSWER(1),
        INVITE(2),
        INVITE_ANSWER(3),
        STORE(4),
        STORE_ANSWER(5),
        FIND_NODE(6),
        FIND_NODE_ANSWER(7),
        FIND_VALUE(8),
        FIND_VALUE_ANSWER(9);

        private final int code;

        /**
         * Constructor. Used only by enum.
         *
         * @param code the value for code field.
         */
        ActionType(int code) {
            this.code = code;
        }

        /**
         * @return the numerical value for the instance
         */
        public int getCode() {
            return code;
        }

        /**
         * @return true if this is a Request Action type, false if it is a Response type
         */
        public boolean isRequest() {
            return (code >= 0) && (code % 2 == 0);
        }

        /**
         * @return true if this is a Request Action type, false if it is a Response type
         */
        public boolean isResponse() {
            return (code >= 0) && (code % 2 != 0);
        }

        /**
         * @param code the code for the Action
         * @return the ActionType with the corresponding code, or INVALID if an invalid code is passed.
         */
        public static ActionType getTypeFromVal(int code) {
            for (ActionType type : ActionType.values()) {
                if (type.code == code) return type;
            }
            return INVALID;
        }
    }

    /**
     * Enum for type of content attached to the Action, if any
     */
    public enum PayloadType {

        INVALID(-1),
        IGNORED(0),
        BOOLEAN(1),
        PEER_ADDRESS(2),
        NODE_ID(3),
        RESOURCE(4);

        private final int code;

        /**
         * Constructor. Used only by enum.
         *
         * @param code the value for code field.
         */
        PayloadType(int code) {
            this.code = code;
        }

        /**
         * @return the numerical value for the instance.
         */
        public int getCode() {
            return code;
        }

        /**
         * @param code the code for the Action
         * @return the ActionType with the corresponding code, or invalid if an invalid code is passed.
         */
        public static PayloadType getTypeFromCode(int code) {
            for (PayloadType type : PayloadType.values()) {
                if (type.code == code) return type;
            }
            return INVALID;
        }
    }

    private enum Token{
        invalidityToken
    }

    /**
     *  Constructor of KadAction.
     *
     * @param actionType The type of the action.
     * @param id The id of the message.
     * @param part The number of the current message part.
     * @param maxParts The number of the total messages.
     * @param payloadType The type of the payload.
     * @param payload The value of the payload.
     * @throws IllegalArgumentException if the parameters are not valid.
     */
    public KadAction(@NonNull SMSPeer smsPeer, @NonNull ActionType actionType, int id, int part, int maxParts, @NonNull PayloadType payloadType, @NonNull String payload) {
        this.actionPeer = smsPeer;
        this.actionType = actionType;
        this.operationId = id;
        this.currentPart = part;
        this.totalParts = maxParts;
        this.payloadType = payloadType;
        this.payload = payload;
        if (!isValid())
            throw new IllegalArgumentException();
    }

    /**
     * Constructor of KadDAction given SMSMessage.
     *
     * @param buildingMessage the given SMSMessage.
     * @throws IllegalArgumentException if the parameters are not valid.
     */
    public KadAction(@NonNull SMSMessage buildingMessage) {
        String messageBody = buildingMessage.getData();
        actionPeer=buildingMessage.getPeer();
        try{
        actionType=ActionType.getTypeFromVal(Integer.parseInt(messageBody.substring(ACTION_TYPE_START_INDEX,OPERATION_ID_START_INDEX)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage() + "\n" + ACTION_CODE_NOT_FOUND_ERROR_MSG);
        }
        operationId=removePadding(messageBody.substring(OPERATION_ID_START_INDEX,CURRENT_PART_START_INDEX));
        currentPart=removePadding(messageBody.substring(CURRENT_PART_START_INDEX,TOTAL_PARTS_START_INDEX));
        totalParts=removePadding(messageBody.substring(TOTAL_PARTS_START_INDEX,PAYLOAD_TYPE_START_INDEX));
        try{
        payloadType=PayloadType.getTypeFromCode(Integer.parseInt(messageBody.substring(PAYLOAD_TYPE_START_INDEX,PAYLOAD_START_INDEX)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage() + "\n" + PAYLOAD_TYPE_NOT_FOUND_ERROR_MSG);
        }
        payload=messageBody.substring(PAYLOAD_START_INDEX);
    }

    /**
     * Constructor that builds an invalid Action
     * @param t token that indicates user's will to build an invalid action
     * @throws IllegalStateException if token is not Token.invalidityToken
     */
    private KadAction(Token t){
        if(t.equals(Token.invalidityToken)){
            this.actionPeer = SMSPeer.INVALID_SMS_PEER;
            this.actionType = ActionType.INVALID;
            this.operationId = 0;
            this.currentPart = 0;
            this.totalParts = 0;
            this.payloadType = PayloadType.INVALID;
            this.payload = "This action is invalid";
        }
        else
            throw  new IllegalStateException(INVALID_ONLY_EXPECTED);
    }

    /**
     * Prevents the user to use the default constructor
     */
    private KadAction(){

    }

    /**
     * Check if all the action's parameters are valid
     *
     * @return True if the defined action is valid and fits into a Message.
     */
    @Override
    public boolean isValid() {
        if (this.actionType.equals(ActionType.INVALID))
            return false;
        if (this.payloadType.equals(PayloadType.INVALID))
            return false;
        if (currentPart <MIN_PARTS || currentPart > totalParts || totalParts>MAX_PARTS )
            return false;
        if(operationId<MIN_ID || operationId>MAX_ID)
            return false;
        if (!payloadMatchesType(payloadType, payload))
            return false;
        try {
            toMessage();
        } catch (InvalidMessageException e) {
            return false;
        }
        return true;
    }

    /**
     * Check if payload's content type matches with payloadType passed as parameter.
     *
     * @param payloadType The type of the payload.
     * @param payload The string which contains the value of the payload.
     * @return True if the payload string contains a value of the correct type,false otherwise.
     */
    public static boolean payloadMatchesType(PayloadType payloadType, String payload) {
        switch (payloadType) {
            case IGNORED:
            case BOOLEAN:
                return true;
            case NODE_ID:
                BitSet bitset=BitSetUtils.decodeHexString(payload);
                return bitset.size()<=ID_LENGTH;
            case PEER_ADDRESS:
                return SMSPeer.isAddressValid(payload) == SMSPeer.PhoneNumberValidity.ADDRESS_VALID;
            case RESOURCE:
                try {
                    return StringResource.parseString(payload, RESOURCE_SEPARATOR).isValid();
                }
                catch (IllegalArgumentException e){
                    return false;
                }
            default:
                return false;
        }

    }

    /**
     * Get a message with correct formatted action command ready to be sent
     *
     * @return A Message containing the formatted action command ready to be sent.
     */
    @Override
    public SMSMessage toMessage() {
        StringBuilder body = new StringBuilder();

        body.append(actionType.getCode()).append(addPadding(operationId,OPERATION_ID_LENGTH))
                .append(addPadding(currentPart,CURRENT_PART_LENGTH)).append(addPadding(totalParts,TOTAL_PARTS_LENGTH))
                .append(payloadType.getCode()).append(payload);

        return new SMSMessage(actionPeer, body.toString());
    }

    /**
     * Get the action type from the enum.
     *
     * @return The type of the action.
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Get the ID which identifies the action.
     *
     * @return The operation ID.
     */
    public int getOperationId() {
        return operationId;
    }
    /**
     * Get the k part of the current message.
     *
     * @return The currentPart k  of the message.
     */

    public int getCurrentPart() {
        return currentPart;
    }
    /**
     * Get the number of the total messages expected .
     *
     * @return The total number of messages.
     */

    public int getTotalParts() {
        return totalParts;
    }
    /**
     * Get the payload type from the enum.
     *
     * @return The type of the payload.
     */

    public PayloadType getPayloadType() {
        return payloadType;
    }

    /**
     * Get the SMSPeer which is sending or receiving the action message.
     *
     * @return SMSPeer the peer sending or receiving the action message.
     */

    @Override
    public SMSPeer getPeer() {
        return actionPeer;
    }

    /**
     * Get a string containing the value of the payload
     *
     * @return A string containing the value of the payload.
     */

    @Override
    public String getPayload() {
        return payload;
    }

    /**
     * Convert an integer to a string and adds to it the padding character.
     *
     * @param intToPadd The integer that need the padding.
     * @param length The wanted length for the string.
     * @return String with the correct length.
     */
     static String addPadding(int intToPadd,int length) {
         String stringToPadd=Integer.toString(intToPadd);
        while(stringToPadd.length()<length)
        {
             stringToPadd=PARSING_CHARACTER+stringToPadd;
        }
        return stringToPadd;
    }

    /**
     * Remove all the paddingCharacter from a String and convert it into an integer.
     * The String has to contain only int.
     *
     * @param string The String you want to remove the padding to.
     * @return String without padding.
     * @throws IllegalArgumentException if the String doesn't contain only integer.
     */
    static int removePadding(String string)
    {
        String expectedString=string;
        int START_OF_THE_STRING=0;
        while (string.charAt(START_OF_THE_STRING)==PARSING_CHARACTER && START_OF_THE_STRING<expectedString.length())
        {
            START_OF_THE_STRING++;
            expectedString=string.substring(START_OF_THE_STRING);
        }
        try {
            return Integer.parseInt(expectedString);
        }
        catch (NumberFormatException e) {
        throw new IllegalArgumentException(e.getMessage());
    }

}
}
