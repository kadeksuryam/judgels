import { shallow, ShallowWrapper } from 'enzyme';
import * as React from 'react';

import { IcpcProblemState, IcpcScoreboard } from '../../../../../../../../../../modules/api/uriel/scoreboard';
import { IcpcScoreboardTable, IcpcScoreboardTableProps } from './IcpcScoreboardTable';

describe('IcpcScoreboardTable', () => {
  let wrapper: ShallowWrapper<IcpcScoreboardTableProps>;

  const scoreboard: IcpcScoreboard = {
    state: {
      problemJids: ['JIDPROG1', 'JIDPROG2', 'JIDPROG3'],
      problemAliases: ['A', 'B', 'C'],
      contestantJids: ['JIDUSER1', 'JIDUSER2'],
    },
    content: {
      entries: [
        {
          rank: 1,
          contestantJid: 'JIDUSER2',
          totalAccepted: 3,
          totalPenalties: 66,
          attemptsList: [1, 3, 1],
          penaltyList: [3, 14, 9],
          problemStateList: [IcpcProblemState.Accepted, IcpcProblemState.FirstAccepted, IcpcProblemState.FirstAccepted],
        },
        {
          rank: 2,
          contestantJid: 'JIDUSER1',
          totalAccepted: 1,
          totalPenalties: 17,
          attemptsList: [1, 1, 0],
          penaltyList: [10, 17, 0],
          problemStateList: [IcpcProblemState.NotAccepted, IcpcProblemState.Accepted, IcpcProblemState.NotAccepted],
        },
      ],
    },
  };

  const contestantDisplayNames = {
    JIDUSER1: 'username1',
    JIDUSER2: 'username2',
  };

  beforeEach(() => {
    const props = { scoreboard, contestantDisplayNames };
    wrapper = shallow(<IcpcScoreboardTable {...props} />);
  });

  test('header', () => {
    const header = wrapper
      .find('thead')
      .find('tr')
      .first()
      .children()
      .map(th => th.text());
    expect(header).toEqual(['#', 'Contestant', 'Total', 'A', 'B', 'C']);
  });

  test('ranks', () => {
    const ranks = wrapper
      .find('tbody')
      .children()
      .map(tr => tr.childAt(0).text());
    expect(ranks).toEqual(['1', '2']);
  });

  test('display names', () => {
    const ranks = wrapper
      .find('tbody')
      .children()
      .map(tr => tr.childAt(1).text());
    expect(ranks).toEqual(['username2', 'username1']);
  });

  test('points', () => {
    const getColor = td =>
      td === undefined
        ? ''
        : td === 'first-accepted' ? 'D ' : td === 'accepted' ? 'G ' : td === 'not-accepted' ? 'R ' : 'X ';
    const mapCell = td => getColor(td.prop('className')) + td.find('strong').text() + '/' + td.find('small').text();
    const mapRow = tr => [2, 3, 4, 5].map(x => tr.childAt(x)).map(mapCell);
    const points = wrapper
      .find('tbody')
      .children()
      .map(mapRow);
    expect(points).toEqual([['3/66', 'G 1/3', 'D 3/14', 'D 1/9'], ['1/17', 'R 1/-', 'G 1/17', 'X -/-']]);
  });
});
