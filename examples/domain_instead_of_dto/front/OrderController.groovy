package org.example.front

class OrderController {
	private static final logger = LoggerFactory.getLogger(this)

	/* will be called by BootStrap.groovy init() */
	void registerJsonMarshallers() {
		registerJsonMarshallers1()
		registerJsonMarshallers2()
	}
	
	@Secured(["ROLE_USER"])
	def checkout() {
		ViewType viewType = ViewType.of(request)
		Channel channel = Channel.of(params['channel'] as Long, viewType)
		Cart cart = params.cartId ? Cart.get(params.cartId as Long) : null
		Long id = params['id'] as Long

		Payment payment = frontOrderService.findPaymentUsingJoin0(id)

		/**
		 * Checkout시에 Payment Status가 101이 아닐경우,
		 * 해당 Cart를 이용하여 Payment를 재생성 한다.
		 */
		if (!payment.isReadyToCheckout()) {
			GiveBackResult.SuccessfullyCreatedPayment.carry(null)
			redirect(url: payment.retryUrl)
		}

		Long accountId = buyerSecurityService.getUser()?.accountId
		BigDecimal pointBalance = billingRequestService.getPointBalance(accountId)
		List<ChannelPayMethod> channelPayMethods = ChannelPayMethod.findAllByChannelAndIsDisplay(channel, Boolean.TRUE).sort() {
			it.payMethod.ordering
		}

		withFormat {
			html {
				String layout = (channel.id == 3L ? 'main.theme3' : 'main.common')
				switch (channel.id) {
					default:
						render(view: "/order/checkout", layout: layout, model: [
								channel           : channel, viewType: viewType,
								title             : '주문하기',
								payment           : payment,
								hasShippingAddress: shippingAddressService.hasUserAddress(accountId),
								addressList : shippingAddressService.findAddressOfUser(accountId, null),
								pointBalance: pointBalance,
								channelPayMethods: channelPayMethods,
								cart : cart
						])
						break
				}
			}
			json {
				render([
					channel: channel, viewType: viewType,
					title: '주문하기',
					payment: payment,
					hasShippingAddress: shippingAddressService.hasUserAddress(accountId),
					addressList : shippingAddressService.findAddressOfUser(accountId, null),
					pointBalance: pointBalance,
					channelPayMethods: channelPayMethods,
					cart : cart
				] as JSON)
			}
		}
	}
	
	@Secured(["ROLE_USER"])
	def list() {
		ViewType viewType = ViewType.of(request)
		Channel channel = Channel.of(params['channel'] as Long, viewType)
		Integer page = params['page'] as Integer ?: params['pageNo'] as Integer ?: 1
		Integer size = params['size'] as Integer ?: params['pageSize'] as Integer ?: 10

		int ITEMS_IN_A_PAGE = 10
		Long accountId = buyerSecurityService.getUser()?.accountId

		withFormat {
			html {
				switch (channel.id) {
					case 1L:
						String layout = 'main.theme1'
						Map counts = [
								theme1: frontOrderService.countPaidPayments(accountId, Channel.THEME1),
								theme2: frontOrderService.countPaidPayments(accountId, Channel.THEME2),
						]
						render(view: "/order/1/list", layout: layout, model: [
								channel: channel, viewType: viewType,
								title  : '주문내역',
								page   : page, size: size,
								counts : counts,
						])
						break
					case 2L:
						String layout = 'main.theme2'
						Map counts = [
								theme1: frontOrderService.countPaidPayments(accountId, Channel.THEME1),
								theme2: frontOrderService.countPaidPayments(accountId, Channel.THEME2),
						]
						render(view: "/order/2/list", layout: layout, model: [
								channel: channel, viewType: viewType,
								title  : '주문내역',
								page   : page, size: size,
								counts : counts,
						])
						break
					default:
						String layout = (channel.id == 3L ? 'main.theme3' : 'main.common')
						Map counts = [
								this: frontOrderService.countPaidPayments(accountId, channel),
						]
						render(view: "/order/list", layout: layout, model: [
								channel: channel, viewType: viewType,
								title  : '주문내역',
								page   : page, size: size,
								counts : counts,
						])
				}
			}
			json {
				/* 주문내역을 가져오는데 지연시간이 길어 ajax 방식으로 구현하고, spinner를 보여준다. */
				PageSearchParam pageParam = new PageSearchParam(pageNo: page, pageSize: size)
				pageParam.pageSize = size ?: ITEMS_IN_A_PAGE
				pageParam.setPageInfo()

				PagedResult<Payment> result = frontOrderService.getPaymentListFast(pageParam, accountId, channel)
				render result.toJson('/order/list')
			}
		}
	}
	
	void registerJsonMarshallers1() {
		JSON.createNamedConfig('/order/list') { config ->
			config.registerObjectMarshaller(Payment) { Payment it ->
				[id      : it.id,
				 payment_id : it.id,
				 paidAt  : it.paidAt,
				 quantity: it.orders*.quantity.sum(),
				 orders  : it.orders,

				 firstOrderItem       : it.orders.first().orderItem, // modified
				 firstReceiverNickname: it.orders.first().receiverNickname,
				 receiversCount       : it.orders*.receiverId.unique().size(),
				]
			}
			config.registerObjectMarshaller(Order) { Order it ->
				String statusLabel = it.extraLabelOfStatus ?
						[it.status.labelForFront, it.extraLabelOfStatus].join(' ') :
						it.status.labelForFront
				[id             : it.id,
				 order_id       : it.id,
				 status         :
						 [id   : it.status.id,
						  name : it.status.name(),
						  label: statusLabel,
						 ],
				 quantity       : it.quantity,
				 payAmount      : it.payAmount.toFormatString(),
				 orderItem      : it.orderItem,
				 orderItemOption: it.orderItemOption,
				]
			}
			config.registerObjectMarshaller(OrderItem) { OrderItem it ->
				VoucherOrderItem voi = it.instanceOf(VoucherOrderItem) ? it as VoucherOrderItem : null
				String exchangeBrandName = voi ? voi.exchangeBrand?.name : null
				[name         : it.name,
				 brandName    : it.brandName ?: exchangeBrandName,
				 imageUrl     : it.imageUrl,
				 channelItemId: it.channelItemId,
				]
			}
			config.registerObjectMarshaller(OrderItemOption) { OrderItemOption it ->
				[itemOptionId : it.itemOptionId,
				 optionContent: it.optionContent,
				 quantity     : it.quantity,
				]
			}
		}
		logger.debug("registerJsonMarshallers1() initialized")
	}
	
	@Secured(["ROLE_USER"])
	def show() {
		ViewType viewType = ViewType.of(request)
		Channel channel = Channel.of(params['channel'] as Long, viewType)
		Long id = params['id'] as Long

		Payment payment = frontOrderService.findPaymentUsingJoin0(id)
		payment = frontOrderService.addDeliveryInOrderDetail(payment)
		payment = frontOrderService.addButtonsInOrderDetail(payment)
		payment.fetchReceiverNickname()

		withFormat {
			html {
				switch (channel.id) {
					case 1L:
						String layout = 'main.theme1'
						render(view: "/order/1/show", layout: layout, model: [
								channel : channel, viewType: viewType,
								title   : '주문내역 상세보기',
								payment : payment,
								giveBack: flash.chainModel?.giveBack
						])
						break
					case 2L:
						String layout = 'main.theme2'
						render(view: "/order/2/show", layout: layout, model: [
								channel : channel, viewType: viewType,
								title   : '주문내역 상세보기',
								payment : payment,
								giveBack: flash.chainModel?.giveBack
						])
						break
					default:
						String layout = (channel.id == 3L ? 'main.theme3' : 'main.common')
						render(view: "/order/show", layout: layout, model: [
								channel : channel, viewType: viewType,
								title   : '주문내역 상세보기',
								payment : payment,
								giveBack: flash.chainModel?.giveBack
						])
				}
			}
		}
	}

}
