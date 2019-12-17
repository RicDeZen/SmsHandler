package com.dezen.riccardo.networkmanager;

import androidx.annotation.NonNull;

import com.dezen.riccardo.networkmanager.exceptions.InvalidActionFormatException;
import com.dezen.riccardo.smshandler.Peer;
import com.dezen.riccardo.smshandler.SMSMessage;
import com.dezen.riccardo.smshandler.SMSPeer;

/**
 * @author Niccolo' Turcato
 * <p>
 * Syntax:
 * <p>
 * <SMSLibrary-TAG>[ACTION]<SEPARATOR>[ARGUMENT]<SEPARATOR>[EXTRA]
 * <p>
 * INVITE [IGNORED] [IGNORED]
 * ACCEPT [IGNORED] [IGNORED]
 * ADD_USER [PEER] [IGNORED]
 * REMOVE_USER [PEER] [IGNORED]
 * GREET_USER [PEER] [IGNORED]
 * MSG [MESSAGE] [IGNORED]
 * REMOVE_RESOURCE [KEY] [IGNORED]
 * ADD_RESOURCE [KEY] [VALUE]
 */
class NetworkAction extends ActionStructure<String> {
    private static final String SEPARATOR = "\r";
    private static final String MSG_SYNTAX_ACTION_ERR = "Parameter action out of range. Expected {0-7}, got: ";
    private static final String MSG_SYNTAX_ARG_ERR = "Parameter \"arg\" can't be empty for this action";
    private static final String MANAGER_LOG_TAG = "NETWORK_MANAGER";
    private static final int ACTION_POSITION = 0, ARG_POSITION = 1, EXTRA_POSITION = 2;
    private static final int NUMBER_OF_PARAM = 3;

    private int actionCommand;
    private String argument;
    private String extra;

    private SMSPeer currentPeer;

    private final String ACTION_CODE_NOT_FOUND_ERROR_MSG = "Expected ActionType as int number, found not parsable String instead";
    private final String FORMATTED_ACTION_NOT_FOUND_ERROR_MSG = "This message does not contain a formatted NetworkAction";
    private final String NOT_FORMATTED_PARAMS = "Params can't build a formatted NetworkAction";
    private final String INVALID_ACTION_SYNTAX_MSG = "This action doesn't meet Syntax Specification";

    public static final String DEFAULT_IGNORED = " ";

    /**
     * @author Riccardo De Zen
     */
    public static class Type {
        /**
         * @param action the int value to be checked
         * @return true if the int matches an action, false if not
         */
        static private boolean isValid(int action) {
            return action >= MIN_ACTION && action <= MAX_ACTION;
        }

        /**
         * @param action the int value to be checked. Actions returning false for isValid also return
         *               false here
         * @return true if the given action uses the "ARGUMENT" part of the message
         */
        static private boolean usesArg(int action) {
            return isValid(action) && action >= ADD_USER;
        }

        /**
         * @param action the int value to be checked. Actions returning false for isValid also return
         *               false here
         * @return true if the given action uses the "EXTRA" part of message
         */
        static private boolean usesExtra(int action) {
            return action == ADD_RESOURCE;
        }

        static private final int MIN_ACTION = 0;
        static private final int MAX_ACTION = 7;

        //[ACTION] [ARGUMENT] [EXTRA]

        //<#>INVITE [IGNORED] [IGNORED]
        static final int INVITE = 0;
        //<#>ACCEPT [IGNORED] [IGNORED]
        static final int ANSWER_INVITE = 1;
        //<#>ADD_USER [PEER] [IGNORED]
        static final int ADD_USER = 2;
        //<#>REMOVE_USER [PEER] [IGNORED]
        static final int REMOVE_USER = 3;
        //<#>GREET_USER [PEER] [IGNORED]
        static final int GREET_USER = 4;
        //<#>MSG [MESSAGE] [IGNORED]
        static final int MSG = 5;
        //<#>REMOVE_RESOURCE [KEY] [IGNORED]
        static final int REMOVE_RESOURCE = 6;
        //<#>ADD_RESOURCE [KEY] [VALUE]
        static final int ADD_RESOURCE = 7;
    }

    /**
     * @param actionType action from NetworkAction.Type.ACATION
     * @param arg        argument of action command
     * @param extraArg   secondary argument
     * @throws IllegalArgumentException if parameters aren't valid
     */
    public NetworkAction(int actionType, @NonNull String arg, @NonNull String extraArg) {
        if (areValidParameters(actionType, arg, extraArg)) {
            actionCommand = actionType;
            argument = arg;
            extra = extraArg;
        } else throw new IllegalArgumentException(NOT_FORMATTED_PARAMS);
    }

    /**
     * @param actionType action from NetworkAction.Type.ACATION
     * @param resource   resource on which the action will be defined (if actionType is coherent)
     * @throws IllegalArgumentException if parameters aren't valid
     */
    public NetworkAction(int actionType, StringResource resource) {
        if (areValidParameters(actionType, resource.getName(), resource.getValue())) {
            actionCommand = actionType;
            argument = resource.getName();
            extra = resource.getValue();
        } else throw new IllegalArgumentException(NOT_FORMATTED_PARAMS);

    }

    /**
     * @param actionType action from NetworkAction.Type.ACATION
     * @param peer       peer on which the action will be defined (if actionType is coherent)
     * @throws IllegalArgumentException if parameters aren't valid
     */
    public NetworkAction(int actionType, SMSPeer peer) {
        if (areValidParameters(actionType, peer.getAddress(), DEFAULT_IGNORED)) {
            actionCommand = actionType;
            argument = peer.getAddress();
            extra = DEFAULT_IGNORED;
        } else throw new IllegalArgumentException(NOT_FORMATTED_PARAMS);
    }


    /**
     * @param actionType action from NetworkAction.Type.ACATION
     * @param arg        argument of action command
     * @param extraArg   secondary argument
     * @return true if parameters have valid format
     */
    private static boolean areValidParameters(int actionType, String arg, String extraArg) {
        //Using XOR operator because arg and extra can't be ignored if they are required
        return Type.isValid(actionType) && (arg.equals(DEFAULT_IGNORED) ^ Type.usesArg(actionType))
                && (extraArg.equals(DEFAULT_IGNORED) ^ Type.usesExtra(actionType));
    }

    /**
     * Builds the action starting from an SMSMessage's body,
     * default currentPeer is set to the buildingMessage.getPeer(), if it exists
     *
     * @param buildingMessage an SMSMessage used to build the action, body must contain ONLY text formatted from this class
     * @throws IllegalArgumentException if buildingMessage's body hasn't expected formatting
     */
    public NetworkAction(@NonNull SMSMessage buildingMessage) {
        String messageBody = buildingMessage.getData();
        String[] params = messageBody.split(SEPARATOR);
        if (buildingMessage.getPeer() != null && buildingMessage.getPeer().isValid())
            currentPeer = buildingMessage.getPeer();
        if (params.length == NUMBER_OF_PARAM) {
            int actionType;
            try {
                actionType = Integer.parseInt(params[ACTION_POSITION]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.getMessage() + "\n" + ACTION_CODE_NOT_FOUND_ERROR_MSG);
            }
            if (areValidParameters(actionType, params[ARG_POSITION], params[EXTRA_POSITION])) {
                actionCommand = actionType;
                argument = params[ARG_POSITION];
                extra = params[EXTRA_POSITION];
            }
        } else throw new IllegalArgumentException(FORMATTED_ACTION_NOT_FOUND_ERROR_MSG);
    }

    public void setDestinationPeer(@NonNull Peer<String> peer) {
        if (peer instanceof SMSPeer && peer.isValid()) {
            currentPeer = (SMSPeer) peer;
        }
    }

    /**
     * @return true if this NetworkAction has been built with parameters that meet defined Syntax, false otherwise
     */
    public boolean isValid() {
        return currentPeer != null && (
                ((actionCommand == Type.INVITE || actionCommand == Type.ANSWER_INVITE)
                        && argument.equals(DEFAULT_IGNORED) && extra.equals(DEFAULT_IGNORED))

                        || ((actionCommand == Type.ADD_USER || actionCommand == Type.GREET_USER || actionCommand == Type.REMOVE_USER)
                        && canBuildPeer(argument) && extra.equals(DEFAULT_IGNORED))

                        || ((actionCommand == Type.MSG) && !argument.equals(DEFAULT_IGNORED) && extra.equals(DEFAULT_IGNORED))
                        || ((actionCommand == Type.ADD_RESOURCE) && !argument.equals(DEFAULT_IGNORED) && !extra.equals(DEFAULT_IGNORED))
                        || ((actionCommand == Type.REMOVE_RESOURCE) && !argument.equals(DEFAULT_IGNORED) && extra.equals(DEFAULT_IGNORED)));
    }

    /**
     * @param phoneNum a string containing a telephone number
     * @return true if you can build an smsPeer with the given phone Number, false otherwise
     */
    private boolean canBuildPeer(String phoneNum) {
        SMSPeer peer;
        try {
            peer = new SMSPeer(phoneNum);
        } catch (Exception e) {
            return false;
        }
        return peer.isValid();
    }

    /**
     * @return a well formatted SMSMessage containing the defined Action
     * @throws InvalidActionFormatException if the built action isn't valid
     */
    public SMSMessage generateMessage() {
        if (isValid()) {
            //Formatted Body
            String body = actionCommand + SEPARATOR + argument + SEPARATOR + extra;
            return new SMSMessage(currentPeer, body);
        } else throw new InvalidActionFormatException(INVALID_ACTION_SYNTAX_MSG);
    }


    public String getParam() {
        return argument;
    }

    public String getExtra() {
        return extra;
    }

    public int getAction() {
        return actionCommand;
    }

    /**
     * @return the Peer that forwarded the action (if the action has been received), the peer that will receive the action (if the action is to send/perform), null if it was not set
     */
    public Peer<String> getPeer() {
        return currentPeer;
    }
}


