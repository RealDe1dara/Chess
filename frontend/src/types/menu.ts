export type ScoreEntry = {
  ident: number
  game: string
  player: string
  points: number
  playedOn: string
}

export type EloEntry = {
  ident: number
  game: string
  player: string
  elo: number
}

export type CommentEntry = {
  ident: number
  game: string
  player: string
  comment: string
  commentedOn: string
}
