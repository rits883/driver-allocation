# Driver Allocation: event-driven extension

This learning extension preserves synchronous allocation and its coordination lock. It emits one immutable outcome per new order (ASSIGNED or REJECTED); repeated identical requests return the stored outcome without creating another event. Invalid or conflicting requests do not create an event.

## Local run
1. Start the existing Kafka Compose services and PostgreSQL. The default single broker is for local use only.
2. Start the app: `ALLOCATION_KAFKA_ENABLED=true ./mvnw spring-boot:run`.
3. Register an ACTIVE driver via POST /api/v1/drivers, then POST /api/v1/orders with {"orderReference":"KAFKA-DEMO-1","orderTime":100,"travelTime":10}. Choose an order time not earlier than the current coordination clock. Repeat the identical request.
4. Inspect `allocation_outbox`, `allocation_inbox` and `allocation_event_projection` in PostgreSQL. Expect one outbox row and two independent projections for a new successful or rejected order once Kafka catches up.

Main topic: driver-allocation-events, key = orderReference. Stable UUID is generated inside the allocation transaction and retained in the outbox payload. The polling relay publishes with acknowledgements and marks the row only after the send completes. A crash before marking may resend; the consumer Inbox protects DB effects. No external SMS/email is sent: the notification projection is a durable intent only.

Audit group: two blocking retries at 2s, then driver-allocation-events-audit-dlt. Notification group: non-blocking retries at 5s/10s/20s, then driver-allocation-events-notification-dlt. Invalid event payload/key is non-retryable. Retry forwarding and offset commit are not atomic; duplicate forwarding remains possible. The DLT handler logs only; no automatic replay.

To inspect:
```sql
SELECT event_id, order_reference, published_at FROM allocation_outbox ORDER BY created_at DESC;
SELECT * FROM allocation_inbox ORDER BY processed_at DESC;
SELECT * FROM allocation_event_projection ORDER BY created_at DESC;
```

Test command (requires the existing dedicated PostgreSQL test database): `./mvnw -Dtest=PersistentAllocationServiceIntegrationTest test`. Tests exercise the outbox and database deduplication; Kafka integration and failure tests still need to be run separately. The old lab listener is opt-in via the kafka-lab profile, not part of normal application operation.

## Limitations
Local single-broker replication factor 1 is not HA. The simple relay holds an outbox-row lock while waiting for Kafka (not the allocation coordination lock); one event per poll is a deliberately low-throughput starting point. A persistently failing oldest row needs alerting and a more advanced retry/claim policy. No cleanup/retention, replay API, external notification dispatch, security, distributed tracing or exactly-once end-to-end guarantee is provided. Do not run production and lab listeners together unless intended. Existing controller tests for the old simulation API are stale.
