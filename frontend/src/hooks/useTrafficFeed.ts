"use client";

import { getLiveSignals, wsBase } from "@/lib/api";
import type { DashboardStats, LiveSignal } from "@/types/traffic";
import { Client, IMessage } from "@stomp/stompjs";
import { useCallback, useEffect, useState } from "react";
import SockJS from "sockjs-client";

export function useTrafficFeed() {
  const [signals, setSignals] = useState<LiveSignal[]>([]);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      const data = await getLiveSignals();
      setSignals(data);
      setError(null);
    } catch {
      setError("API unreachable — start Spring Boot (port 8080)");
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () =>
        new SockJS(`${wsBase()}/ws`) as unknown as WebSocket,
      reconnectDelay: 5000,
      debug: () => {},
      onConnect: () => {
        setConnected(true);
        client.subscribe("/topic/signals", (msg: IMessage) => {
          try {
            setSignals(JSON.parse(msg.body) as LiveSignal[]);
          } catch {
            /* ignore */
          }
        });
        client.subscribe("/topic/stats", (msg: IMessage) => {
          try {
            setStats(JSON.parse(msg.body) as DashboardStats);
          } catch {
            /* ignore */
          }
        });
      },
      onDisconnect: () => setConnected(false),
      onWebSocketClose: () => setConnected(false),
    });
    client.activate();
    return () => {
      void client.deactivate();
    };
  }, []);

  return { signals, stats, connected, error, refresh };
}
