package com.oinotna.umbra.input.keyboard;

import com.oinotna.umbra.input.Command;
import com.oinotna.umbra.input.InputManager;

import java.nio.ByteBuffer;

public class Keyboard {
    private static final byte KEY_DOWN= 0x01;
    private static final byte KEY_UP= 0x02;
    public static class KeyboardCommand extends Command {
        private final int action;
        private Integer value;
        public KeyboardCommand(int action){
            super(InputManager.KEYBOARD);
            this.action=action;
        }

        public KeyboardCommand(int action, int value){
            super(InputManager.KEYBOARD);
            this.action=action;
            this.value=value;
        }

        @Override
        public byte[] getCommandBytes() {
            ByteBuffer command;
            if(value != null){
                command = ByteBuffer.allocate( 1 + 4 + 4);
                command.put(this.getType());
                command.putInt(action);
                command.putInt(value);

            }
            else{
                command = ByteBuffer.allocate(1+4);
                command.put(this.getType());
                command.putInt(action);
            }
            return command.array();
        }
    }

    public static void sendKeyDown(int i){
        InputManager.push(new KeyboardCommand(KEY_DOWN, i));
    }
    public static void sendKeyUp(int i){
        InputManager.push(new KeyboardCommand(KEY_UP, i));
    }
}
