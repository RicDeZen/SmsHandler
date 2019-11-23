package com.dezen.riccardo.networkmanager;

/**
 * Interface meant to represent a resource in the peersVocabulary instead
 * of a message travelling through the network.
 * @author Riccardo De Zen.
 * TODO As suggested by Mattia Fanan, Resource should be an abstract class to allow guaranteed
 *  overriding of equals and compareTo, for better testing and generic Dictionary.
 */
public interface Resource<N, V>{
    N getName();
    V getValue();
}
