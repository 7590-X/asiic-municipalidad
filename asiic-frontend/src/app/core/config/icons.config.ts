import '@cds/core/icon/register.js';
import {
  ClarityIcons,
  angleIcon,
  barsIcon,
  bellIcon,
  calendarIcon,
  checkCircleIcon,
  cogIcon,
  ellipsisVerticalIcon,
  exclamationCircleIcon,
  exclamationTriangleIcon,
  folderIcon,
  helpInfoIcon,
  homeIcon,
  infoCircleIcon,
  searchIcon,
  timesIcon,
  userIcon,
  viewColumnsIcon,
} from '@cds/core/icon';
import { logoutIcon } from '@cds/core/icon/shapes/logout.js';
import { fileIcon } from '@cds/core/icon/shapes/file.js';
import { plusCircleIcon } from '@cds/core/icon/shapes/plus-circle.js';
import { shieldIcon } from '@cds/core/icon/shapes/shield.js';
import { historyIcon } from '@cds/core/icon/shapes/history.js';
import { talkBubblesIcon } from '@cds/core/icon/shapes/talk-bubbles.js';
import { idBadgeIcon } from '@cds/core/icon/shapes/id-badge.js';
import { envelopeIcon } from '@cds/core/icon/shapes/envelope.js';
import { refreshIcon } from '@cds/core/icon/shapes/refresh.js';
import { filterIcon } from '@cds/core/icon/shapes/filter.js';

export function initializeClarityIcons(): void {
  ClarityIcons.addIcons(
    angleIcon,
    barsIcon,
    bellIcon,
    calendarIcon,
    checkCircleIcon,
    cogIcon,
    ellipsisVerticalIcon,
    exclamationCircleIcon,
    exclamationTriangleIcon,
    folderIcon,
    helpInfoIcon,
    homeIcon,
    infoCircleIcon,
    searchIcon,
    timesIcon,
    userIcon,
    viewColumnsIcon,
    logoutIcon,
    fileIcon,
    plusCircleIcon,
    shieldIcon,
    historyIcon,
    talkBubblesIcon,
    idBadgeIcon,
    envelopeIcon,
    refreshIcon,
    filterIcon
  );
}
