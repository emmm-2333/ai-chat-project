# Transform AI Chat to DeepSeek Style with Streaming & Theme Switching

I will transform the application to support streaming responses and a DeepSeek-inspired UI, with a manual toggle for Light/Dark modes.

## 1. Backend: Enable Streaming Support
- **Modify `ChatService`**:
  - Add `streamMessage` method returning `Flux<String>`.
  - Update `WebClient` to use `stream: true` and `.bodyToFlux()`.
  - Accumulate streamed content and save to DB upon completion to ensure history persistence.
- **Update `ChatController`**:
  - Add `POST /api/chat/{conversationId}/stream` endpoint for Server-Sent Events (SSE).

## 2. Frontend: UI Redesign & Theme System
- **Dependencies**: Install `markdown-it`, `highlight.js`.
- **Theme System**:
  - Implement a **Theme Toggle** (Light/Dark) using CSS variables.
  - **Dark Mode (DeepSeek)**: Background `#15171a`, Sidebar `#1f2226`, Input `#2b2d31`.
  - **Light Mode**: Clean white/gray scheme.
  - Persist theme preference in `localStorage`.
- **UI Layout**:
  - **Sidebar**: Dark/Light adaptive, clean conversation list.
  - **Main Area**: Centered chat container.
  - **Input Area**: Floating, rounded input box at the bottom.
- **Streaming Logic**:
  - Use `fetch` API to consume the stream.
  - Render Markdown content in real-time.

## 3. Verification
- Verify streaming response works (token-by-token).
- Verify UI matches DeepSeek style in Dark mode.
- Verify Theme Toggle switches correctly and persists.
