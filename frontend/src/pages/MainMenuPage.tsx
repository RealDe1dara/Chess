import { useCallback, useEffect, useMemo, useState } from 'react'
import MenuHeader from '../components/menu/MenuHeader'
import ProfileModal from '../components/menu/ProfileModal'
import ToastStack from '../components/menu/ToastStack'
import PlayerStatsSection from '../components/menu/PlayerStatsSection'
import LastGamesSection from '../components/menu/LastGamesSection'
import TopPlayersSection from '../components/menu/TopPlayersSection'
import CommentsCard from '../components/menu/CommentsCard'
import useToasts from '../hooks/useToasts'
import type { User } from '../types/auth'
import type { CommentEntry, EloEntry, ScoreEntry } from '../types/menu'
import '../css/menu/main-menu-page.css'

const GAME = 'chess'
const LAST_GAMES_LIMIT = 20

type LastGameView = {
  id: string
  playedOn: string
  pair: string
  result: 'Won' | 'Lost' | 'Draw' | 'Played'
}

type MainMenuPageProps = {
  user: User
  onUserChange: (user: User | null) => void
  onLogout: () => void
}

function MainMenuPage({ user, onUserChange, onLogout }: MainMenuPageProps) {
  const [elo, setElo] = useState<number | null>(null)
  const [userRating, setUserRating] = useState<number | null>(null)
  const [averageRating, setAverageRating] = useState<number | null>(null)
  const [topElo, setTopElo] = useState<EloEntry[]>([])
  const [playerScores, setPlayerScores] = useState<ScoreEntry[]>([])
  const [recentScores, setRecentScores] = useState<ScoreEntry[]>([])
  const [comments, setComments] = useState<CommentEntry[]>([])
  const [commentDraft, setCommentDraft] = useState('')
  const [profileOpen, setProfileOpen] = useState(false)
  const { toasts, pushToast } = useToasts()

  const readNumericResponse = useCallback(async (response: Response): Promise<number | null> => {
    if (!response.ok) {
      return null
    }
    try {
      return (await response.json()) as number
    } catch {
      return null
    }
  }, [])

  const readListResponse = useCallback(async <T,>(response: Response): Promise<T[]> => {
    if (!response.ok) {
      return []
    }
    try {
      return (await response.json()) as T[]
    } catch {
      return []
    }
  }, [])

  const loadMenuData = useCallback(async () => {
    const encodedName = encodeURIComponent(user.username)
    const [eloResponse, userRatingResponse, averageRatingResponse, topEloResponse, playerScoresResponse, recentScoresResponse, commentsResponse] =
      await Promise.all([
        fetch(`/api/elo/${GAME}/${encodedName}`, { credentials: 'include' }),
        fetch(`/api/rating/${GAME}/${encodedName}`, { credentials: 'include' }),
        fetch(`/api/rating/average/${GAME}`, { credentials: 'include' }),
        fetch(`/api/elo/top/${GAME}`, { credentials: 'include' }),
        fetch(`/api/score/player/${GAME}/${encodedName}?limit=30`, { credentials: 'include' }),
        fetch(`/api/score/recent/${GAME}?limit=200`, { credentials: 'include' }),
        fetch(`/api/comment/${GAME}`, { credentials: 'include' }),
      ])

    setElo(await readNumericResponse(eloResponse))
    setUserRating(await readNumericResponse(userRatingResponse))
    setAverageRating(await readNumericResponse(averageRatingResponse))
    setTopElo(await readListResponse<EloEntry>(topEloResponse))
    setPlayerScores(await readListResponse<ScoreEntry>(playerScoresResponse))
    setRecentScores(await readListResponse<ScoreEntry>(recentScoresResponse))
    setComments(await readListResponse<CommentEntry>(commentsResponse))
  }, [readListResponse, readNumericResponse, user.username])

  const addComment = async () => {
    const trimmedComment = commentDraft.trim()
    if (!trimmedComment) {
      pushToast('Comment cannot be empty.', 'error')
      return
    }

    const response = await fetch('/api/comment', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        game: GAME,
        player: user.username,
        comment: trimmedComment,
        commentedOn: new Date().toISOString(),
      }),
    })

    if (!response.ok) {
      pushToast('Unable to add comment.', 'error')
      return
    }

    setCommentDraft('')
    pushToast('Comment added.')
    await loadMenuData()
  }

  const buildScoreResult = useCallback(
    (playerScore: ScoreEntry): LastGameView['result'] => {
      const key = new Date(playerScore.playedOn).toISOString()
      const pair = recentScores.filter((entry) => new Date(entry.playedOn).toISOString() === key)
      const opponentScore = pair.find((entry) => entry.player !== user.username)
      if (opponentScore) {
        if (playerScore.points > opponentScore.points) {
          return 'Won'
        }
        if (playerScore.points < opponentScore.points) {
          return 'Lost'
        }
        return 'Draw'
      }
      if (playerScore.points > 0) {
        return 'Won'
      }
      if (playerScore.points < 0) {
        return 'Lost'
      }
      if (playerScore.points === 0) {
        return 'Draw'
      }
      return 'Played'
    },
    [recentScores, user.username],
  )

  const handleSetUserRating = async (value: number) => {
    const response = await fetch('/api/rating', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        game: GAME,
        player: user.username,
        rating: value,
        ratedOn: new Date().toISOString(),
      }),
    })

    if (!response.ok) {
      pushToast('Unable to set rating.', 'error')
      return
    }

    pushToast('Rating updated.')
    await loadMenuData()
  }

  const lastGames = useMemo<LastGameView[]>(() => {
    const groupedByTime = new Map<string, ScoreEntry[]>()
    for (const score of recentScores) {
      const key = new Date(score.playedOn).toISOString()
      const bucket = groupedByTime.get(key)
      if (bucket) {
        bucket.push(score)
      } else {
        groupedByTime.set(key, [score])
      }
    }

    return playerScores.slice(0, LAST_GAMES_LIMIT).map((playerScore) => {
      const key = new Date(playerScore.playedOn).toISOString()
      const pair = groupedByTime.get(key) ?? []
      const opponentScore = pair.find((entry) => entry.player !== user.username)
      const opponent = opponentScore?.player ?? 'Unknown'

      return {
        id: `${playerScore.ident}-${key}`,
        playedOn: playerScore.playedOn,
        pair: `${user.username} vs ${opponent}`,
        result: buildScoreResult(playerScore),
      }
    })
  }, [buildScoreResult, playerScores, recentScores, user.username])

  const statsSummary = useMemo(() => {
    const results = playerScores.map(buildScoreResult)
    const wins = results.filter((result) => result === 'Won').length
    const draws = results.filter((result) => result === 'Draw').length
    const losses = results.filter((result) => result === 'Lost').length
    return { wins, draws, losses, totalGames: playerScores.length }
  }, [buildScoreResult, playerScores])

  useEffect(() => {
    const timerId = window.setTimeout(() => {
      void loadMenuData()
    }, 0)
    return () => window.clearTimeout(timerId)
  }, [loadMenuData])

  return (
    <section className="main-menu-page">
      <MenuHeader averageRating={averageRating} onProfileOpen={() => setProfileOpen(true)} onLogout={onLogout} />

      <ProfileModal
        user={user}
        elo={elo}
        open={profileOpen}
        onClose={() => setProfileOpen(false)}
        onNotify={pushToast}
        onUserChange={(updatedUser) => {
          onUserChange(updatedUser)
          pushToast(`Signed in as ${updatedUser.username}.`)
        }}
      />

      <div className="menu-grid">
        <div className="left-column-stack">
          <PlayerStatsSection
            username={user.username}
            elo={elo}
            userRating={userRating}
            totalGames={statsSummary.totalGames}
            wins={statsSummary.wins}
            draws={statsSummary.draws}
            losses={statsSummary.losses}
            onSetRating={handleSetUserRating}
          />
        <TopPlayersSection topElo={topElo} currentUsername={user.username} />
        </div>
        <LastGamesSection games={lastGames} onNewGame={() => pushToast('New game is coming soon.')} />
          <CommentsCard
            comments={comments}
            draft={commentDraft}
            onDraftChange={setCommentDraft}
            onAddComment={() => void addComment()}
          />
      </div>

      <ToastStack toasts={toasts} />
    </section>
  )
}

export default MainMenuPage
