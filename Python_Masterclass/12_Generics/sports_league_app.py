"""
============================================================================
 SECTION 12 : REAL-WORLD APP - Type-safe Sports League (Python edition)
============================================================================

 Same design as SportsLeagueApp.java, expressed with typing generics:
   P = TypeVar("P", bound=Player)       players must satisfy the Player protocol
   class Team(Generic[P])               a team of ONE player type
   TeamT = TypeVar("TeamT", bound=Team) league of one team type
   class League(Generic[TeamT])
   class Repository(Generic[ID, E])     generic in-memory store, reused

 Run mypy on this file and the commented-out mistake below is reported, just
 like javac. At runtime Python happily runs it, which is the whole lesson.

 HOW TO RUN:  python3 sports_league_app.py
============================================================================
"""
from collections.abc import Callable
from dataclasses import dataclass
from functools import total_ordering
from typing import Generic, Protocol, TypeVar


class Player(Protocol):
    # read-only properties, so frozen dataclasses (immutable attributes) satisfy the protocol
    @property
    def name(self) -> str: ...

    @property
    def rating(self) -> int: ...


@dataclass(frozen=True)
class CricketPlayer:
    name: str
    role: str
    rating: int


@dataclass(frozen=True)
class FootballPlayer:
    name: str
    position: str
    rating: int


P = TypeVar("P", bound=Player)


@total_ordering
class Team(Generic[P]):
    def __init__(self, name: str):
        self.name = name
        self.roster: list[P] = []
        self.played = self.won = self.drawn = self.lost = self.scored = self.conceded = 0

    def sign(self, player: P) -> "Team[P]":
        self.roster.append(player)
        return self

    @property
    def points(self) -> int:
        return self.won * 3 + self.drawn

    @property
    def diff(self) -> int:
        return self.scored - self.conceded

    def avg_rating(self) -> float:
        return sum(p.rating for p in self.roster) / len(self.roster)

    def star(self) -> P:                                     # returns the CONCRETE player type
        return max(self.roster, key=lambda p: p.rating)

    def record(self, gf: int, ga: int) -> None:
        self.played += 1
        self.scored += gf
        self.conceded += ga
        if gf > ga:
            self.won += 1
        elif gf == ga:
            self.drawn += 1
        else:
            self.lost += 1

    def _key(self):
        return (-self.points, -self.diff, self.name)

    def __eq__(self, other) -> bool:
        return isinstance(other, Team) and self._key() == other._key()

    def __lt__(self, other: "Team") -> bool:                  # sorts best team first
        return self._key() < other._key()


TeamT = TypeVar("TeamT", bound=Team)


class League(Generic[TeamT]):
    def __init__(self, title: str):
        self.title = title
        self.teams: list[TeamT] = []

    def add(self, team: TeamT) -> None:
        self.teams.append(team)

    def play_round_robin(self) -> None:
        for i, a in enumerate(self.teams):
            for j in range(i + 1, len(self.teams)):
                b = self.teams[j]
                ga = round(a.avg_rating() / 30 + (i + j) % 2)
                gb = round(b.avg_rating() / 30 + (i * j) % 2)
                a.record(ga, gb)
                b.record(gb, ga)

    def print_table(self) -> None:
        print(f"=== {self.title} ===")
        print("  Pos Team              P  W  D  L  GD  Pts  Star player")
        for pos, t in enumerate(sorted(self.teams), 1):
            print(f"  {pos:2d}. {t.name:<16} {t.played:2d} {t.won:2d} {t.drawn:2d} {t.lost:2d} {t.diff:+3d}  {t.points:3d}  {t.star().name}")


ID = TypeVar("ID")
E = TypeVar("E")


class Repository(Generic[ID, E]):
    def __init__(self):
        self._store: dict[ID, E] = {}

    def save(self, key: ID, entity: E) -> None:
        self._store[key] = entity

    def find_by_id(self, key: ID) -> E | None:
        return self._store.get(key)

    def find_all(self, pred: Callable[[E], bool] = lambda e: True) -> list[E]:
        return [e for e in self._store.values() if pred(e)]


def roster_summary(team: Team) -> str:
    return f"{team.name}: " + " ".join(f"{p.name}({p.rating})" for p in team.roster)


def main() -> None:
    chennai = Team[FootballPlayer]("Chennai FC").sign(FootballPlayer("Arjun", "FW", 82)).sign(FootballPlayer("Vikram", "GK", 78))
    goa = Team[FootballPlayer]("Goa Waves").sign(FootballPlayer("Rohan", "MF", 88)).sign(FootballPlayer("Dev", "DF", 75))
    kerala = Team[FootballPlayer]("Kerala Tuskers").sign(FootballPlayer("Nikhil", "FW", 70)).sign(FootballPlayer("Sam", "MF", 72))
    mumbai = Team[FootballPlayer]("Mumbai City").sign(FootballPlayer("Kabir", "FW", 85)).sign(FootballPlayer("Ishan", "DF", 81))
    # chennai.sign(CricketPlayer("x", "BAT", 90))   # mypy: incompatible type "CricketPlayer"; expected "FootballPlayer"

    isl = League[Team[FootballPlayer]]("Super League (football)")
    for ft in (chennai, goa, kerala, mumbai):
        isl.add(ft)
    isl.play_round_robin()
    isl.print_table()

    csk = Team[CricketPlayer]("Chennai Kings").sign(CricketPlayer("Ruturaj", "BAT", 86)).sign(CricketPlayer("Jadeja", "AR", 90))
    mi = Team[CricketPlayer]("Mumbai Blues").sign(CricketPlayer("Rohit", "BAT", 89)).sign(CricketPlayer("Bumrah", "BOWL", 95))
    rcb = Team[CricketPlayer]("Bengaluru Reds").sign(CricketPlayer("Virat", "BAT", 93)).sign(CricketPlayer("Siraj", "BOWL", 84))
    ipl = League[Team[CricketPlayer]]("T20 League (cricket)")
    for ct in (csk, mi, rcb):
        ipl.add(ct)
    ipl.play_round_robin()
    print()
    ipl.print_table()

    star = mi.star()                                         # inferred as CricketPlayer -> .role is known
    print(f"\n  Mumbai Blues star: {star.name} ({star.role})")

    print("\n=== ONE FUNCTION FOR ANY TEAM ===")
    print("  " + roster_summary(goa))
    print("  " + roster_summary(rcb))

    print("\n=== ONE GENERIC REPOSITORY ===")
    players: Repository[str, Player] = Repository()
    for team in (chennai, goa, csk, mi, rcb):
        for p in team.roster:
            players.save(p.name.lower(), p)
    print(f"  find_by_id('bumrah') -> {players.find_by_id('bumrah')}")
    print(f"  find_by_id('messi')  -> {players.find_by_id('messi') or 'not found'}")
    print(f"  rated 90+: {[p.name for p in players.find_all(lambda p: p.rating >= 90)]} | total: {len(players.find_all())}")


if __name__ == "__main__":
    main()
