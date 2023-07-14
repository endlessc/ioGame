/*
 * ioGame
 * Copyright (C) 2021 - 2023  渔民小镇 （262610965@qq.com、luoyizhu@gmail.com） . All Rights Reserved.
 * # iohao.com . 渔民小镇
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.iohao.game.external.client.input;

import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.common.kit.StrKit;
import com.iohao.game.external.client.kit.InputCommandKit;
import com.iohao.game.external.client.kit.ScannerKit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 玩家模拟命令管理器
 * <p>
 * 职责
 * <pre>
 *     添加模拟请求
 *     执行请求
 * </pre>
 *
 * @author 渔民小镇
 * @date 2023-07-08
 */
@Slf4j
public class ClientUserInputCommands {
    final AtomicBoolean starting = new AtomicBoolean();
    @Getter
    final ClientUserChannel clientUserChannel;


    Map<String, InputCommand> inputCommandMap = new LinkedHashMap<>();

    public ClientUserInputCommands(ClientUserChannel clientUserChannel) {
        this.clientUserChannel = clientUserChannel;
    }

    private void addCommand(InputCommand inputCommand) {

        Objects.requireNonNull(inputCommand);
        String inputName = inputCommand.getInputName();

        inputCommandMap.put(inputName, inputCommand);
    }

    public String toInputName(CmdInfo cmdInfo) {
        return InputCommandKit.toInputName(cmdInfo);
    }

    public InputCommand ofCommand(CmdInfo cmdInfo) {
        InputCommand inputCommand = new InputCommand(cmdInfo);
        addCommand(inputCommand);
        return inputCommand;
    }

    public InputCommand getInputCommand(String inputName) {
        return inputCommandMap.get(inputName);
    }

    public InputCommand getInputCommand(CmdInfo cmdInfo) {
        String inputName = toInputName(cmdInfo);
        return inputCommandMap.get(inputName);
    }

    public RequestCommand ofRequestCommand(CmdInfo cmdInfo) {
        InputCommand inputCommand = this.getInputCommand(cmdInfo);
        return new RequestCommand(inputCommand, this);
    }

    public void request(CmdInfo cmdInfo) {
        String inputName = InputCommandKit.toInputName(cmdInfo);
        request(inputName);
    }

    /**
     * 向服务器发起请求
     *
     * @param inputName 请求命令
     */
    void request(String inputName) {
        InputCommand inputCommand = this.getInputCommand(inputName);
        if (Objects.isNull(inputCommand)) {
            System.err.printf("【%s】命令不存在\n", inputName);
            return;
        }

        System.out.println(inputCommand);

        try {
            // 发起请求
            clientUserChannel.request(inputCommand);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    public void help() {
        System.out.println("---------- cmd help ----------");
        inputCommandMap.forEach((s, inputCommand) -> System.out.println(inputCommand.toString()));
        System.out.println("------------------------------");
    }

    public void listenHelp() {
        System.out.println("---------- 广播监听 help ----------");
        clientUserChannel.getListenBroadcastMap().values().forEach(System.out::println);
        System.out.println("------------------------------");
    }

    public void start() {
        if (starting.get()) {
            return;
        }

        if (!starting.compareAndSet(false, true)) {
            return;
        }

        // 启动通信 channel
        clientUserChannel.startup();

//        InternalKit.execute(this::extracted);
        String simpleName = this.getClass().getSimpleName();
//        ExecutorKit.newSingleThreadExecutor(simpleName).execute(this::extracted);
    }

    private void extracted() {
        String input = "";

        while (!input.equalsIgnoreCase("q")) {

            System.out.println("提示：[命令执行 : cmd-subCmd] [退出 : q] [帮助 : help]");

            input = ScannerKit.scanner.nextLine();
            input = input.trim();

            if (StrKit.isEmpty(input)) {
                continue;
            }

            if (Objects.equals(input, "help") || Objects.equals(input, ".")) {
                help();
                continue;
            }

            if (Objects.equals(input, "..")) {
                listenHelp();
                continue;
            }

            if (Objects.equals(input, "...")) {
                help();
                listenHelp();
                continue;
            }

            if (Objects.equals(input, "q")) {
                System.out.println("88，老哥！顺便帮忙关注一下组织 https://github.com/game-town");
                System.exit(-1);
                continue;
            }

            // 发起模拟请求
            request(input);
        }
    }
}