/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.anim.fsm;

/**
 *
 * @author capdevon
 */
public enum AnimatorConditionMode {
    If,         //The condition is true when the parameter value is true.
    IfNot,      //The condition is true when the parameter value is false.
    Greater,    //The condition is true when parameter value is greater than the threshold.
    Less,       //The condition is true when the parameter value is less than the threshold.
    Equals,     //The condition is true when parameter value is equal to the threshold.
    NotEqual    //The condition is true when the parameter value is not equal to the threshold.
}
