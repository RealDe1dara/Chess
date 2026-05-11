export type GameMode = 'LOCAL' | 'REMOTE' | 'COMPUTER'
export type Difficulty = 'EASY' | 'MID' | 'HARD' | 'EXPERT'
export type GameStatus = 'WAITING' | 'ACTIVE' | 'FINISHED'
export type GameResult = 'WHITE_WON' | 'BLACK_WON' | 'DRAW' | null
export type TurnColor = 'WHITE' | 'BLACK'
export type WinReason = 'CHECKMATE' | 'RESIGN' | 'TIMEOUT' | null
export type DrawReason = 'STALEMATE' | 'INSUFFICIENT_MATERIAL' | 'THREEFOLD_REPETITION' | 'AGREEMENT' | null

export type ChatMessage = {
  sender: string
  text: string
  sentAt: number
}

export type GameState = {
  id: number
  playerWhite: string
  playerBlack: string | null
  mode: GameMode
  status: GameStatus
  result: GameResult
  winReason: WinReason
  drawReason: DrawReason
  currentTurn: TurnColor | null
  board: (string | null)[][] | null
  promotionPending: boolean
  drawProposedBy: string | null
  message: string
  chatMessages: ChatMessage[]
  whiteInCheck: boolean
  blackInCheck: boolean
  timeLimitSeconds: number | null
  timeWhiteMs: number | null
  timeBlackMs: number | null
  computerDifficulty: Difficulty | null
  lastMoveFrom: string | null
  lastMoveTo: string | null
}
